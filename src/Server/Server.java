package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    public static void main(String[] args) {
        {
            ArrayList<User> users = new ArrayList<>();//Коллекция с Клиентами
            try {
                ServerSocket serverSocket = new ServerSocket(8188);//Создаём серверный сокет
                System.out.println("Сервер запущен");
                while (true) {//Бесконечный цикл для ожидания подключения Клиентов
                    Socket socket = serverSocket.accept();//Ожидаем подключения клиента
                    System.out.println("Клиент подключился");
                    User currentUser = new User(socket);//создаём класс Пользователя
                    users.add(currentUser);
                    DataInputStream in = new DataInputStream(currentUser.getSocket().getInputStream());//это поток ввода
                    DataOutputStream out = new DataOutputStream(currentUser.getSocket().getOutputStream());//это поток вывода
//это третий способ многопоточности
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                out.writeUTF("Добро пожаловать на Сервер");
                                String userName;
                                while (true) {//бесконечный цикл, где пользователь должен ввести уникальное имя в системе. Пока не будет введено уникальное имя в системе, дальше не пойдём
                                    out.writeUTF("Введите Ваше имя: ");
                                    userName = in.readUTF();//Ожидаем имя от Клиента
                                    for (User user : users) {//проверям, что такое имя пользователя не используется. Если используется, то не даём пользователю зарегистрироваться под этим именем и отсылаем пользователю соответствующее сообщение
                                        if (user.getUserName() != null && user.getUserName().equals(userName)) {//делаем проверку на то, что имя пользователя не null, т.к. User уже создано, но userName ещё не присвоено
                                            out.writeUTF("К сожалению, имя " + userName + " уже занято, введите другое имя.");
                                            System.out.println("Пользователь пытается ввести уже используемое имя: " + userName);//Выводим в консоль сервера сообщение о том, что кто-то пытается зайти под именем, которое уже есть в системе
                                            userName = null;//присваиваем null, чтобы продолжить бесконечный цикл
                                            break;//не идём дальше перебирать пользователей, возвращаеся на момент приглашения на ввод имени
                                        }
                                    }
                                    if (userName != null)
                                        break;//выходим из бесконечного цикла только, если имя Полтзователя задано, т.е. оно не null
                                }
                                currentUser.setUserName(userName);
                                System.out.println(currentUser.getUserName() + " теперь в чате.");//Выводим сообщение об имени пользователя, который вошёл в чат в консоль сервера(логирование действий на Сервере)
                                broadcastMessage(currentUser.getUserName() + " теперь в чате.");//Отправка сообщения всем доступным Клиентам
                                out.writeUTF("Вы вошли в чат.");
                                while (true) {
                                    String request = in.readUTF();//читаем сообщения от Клиента
                                    if (!request.startsWith("/")) {//Если сообщение не начинается на /, то отправляем сообщение всем
                                        broadcastMessage(currentUser.getUserName() + ": " + request);//Отправка сообщения всем доступным Клиентам
                                        System.out.println(currentUser.getUserName() + ": " + request);//Логирование действий пользователя на Сервере
                                        continue;
                                    }
                                    //<command> <message> - сообщения с системными командами начинаются на /
                                    // /m ivan отправляет сообщение только пользователю ivan
                                    String[] tokens = request.split(" ");//разделяем сообщение по пробелу
                                    if (tokens[0].equals("/m") && tokens.length > 2) {//если ввели только /m пробел, но далее ничего не ввели отправлем сообщение пользователю об ошибке. Чтобы не было ошибки пользователю надо ввести как минимум /m ivan сообщение
                                        request = "";
                                        for (int i = 2; i < tokens.length; i++) {//собираем сообщение назад. Т.е. вырезаем из сообщения /m ivan
                                            request += tokens[i] + " ";
                                        }
                                        System.out.println(currentUser.getUserName() + " только для " + tokens[1] + ": " + request);//Логирование действий пользователя на Сервере
                                        sendMessageToClient(request, tokens[1]);
                                        continue;
                                    } else {//если ввели впереди сообщения слеш /, но далее не ввели w, от отправляем пользователю сообщение об ошибке
                                        out.writeUTF("Не хватает параметров, необходимо отправить команду следующего вида: /m <имя пользователя> <сообщение для пользователя>");
                                        continue;
                                    }
                                }
                            } catch (IOException e) {
                                users.remove(currentUser);//удаляем Клиента из коллекции Пользователей
                                System.out.println(currentUser.getUserName() + " теперь НЕ в чате.");//Выводим сообщение об имени пользователя, который вышел из чата в консоль сервера(логирование действий на Сервере)
                                try {
                                    broadcastMessage(currentUser.getUserName() + " теперь НЕ в чате.");//Отправка сообщения всем доступным Клиентам
                                } catch (IOException ex) {
                                    ex.printStackTrace();
                                }
                            }
                        }

                        //Метод broadcastMessage - отправляет сообщение всем пользователям, кроме того, который это сообщение отправил.
                        //String request - сам текст сообщения
                        private void broadcastMessage(String request) throws IOException {
                            for (User user : users) {
                                //выяснилась особенность, что пользователь подключился к серверу, но ещё не ввёл имени. Но ему при этом тоже будут приходить сообщения.
                                //добавил проверку user.getUserName() == null и теперь пользователю, который не ввёл ещё имени ничего приходить не будет
                                if (users.indexOf(currentUser) == users.indexOf(user) || user.getUserName() == null)
                                    continue;//Не отправляем сообщение самому пользователю, который отправил это сообщение
                                DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                out.writeUTF(request);
                            }
                        }

                        //Метод sendMessageToClient - отправляет сообщение только конкретному пользователя
                        //String request - сам текст сообщения
                        //userName - имя пользователя, которому отправляем сообщение
                        //Пользователь может отправить сообщение самому себе
                        private void sendMessageToClient(String request, String userName) throws IOException {
                            for (User user : users) {
                                //выяснилась особенность, что пользователь подключился к серверу, но ещё не ввёл имени. Но ему при этом тоже будут приходить сообщения.
                                //добавил проверку user.getUserName() == null и теперь пользователю, который не ввёл ещё имени ничего приходить не будет
                                if (user.getUserName() == null) continue;
                                if (user.getUserName().equals(userName)) {
                                    DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                    out.writeUTF(request);
                                    return;//прерываем цикл и выходим из метода, т.к. нет смысла идти далее, т.к. нужный пользователь найден и сообщение ему отправлено
                                }
                            }
                            //попадаем сюда, если перебрали всех пользователей и нужный пользователь не найден
                            out.writeUTF("Пользователь " + userName + " не найден, сообщение не отправлено.");
                        }
                    }
                    );
                    thread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

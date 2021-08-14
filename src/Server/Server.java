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
                                System.out.println(currentUser.getUserName() + " теперь в чате.");//Выводим сообщение об имени пользователя, который вошёл в чат в консоль чата
                                for (User user : users) {
                                    if (users.indexOf(currentUser) == users.indexOf(user)) {//Не отправляем сообщение самому пользователю, который вошёл в чат. Но отправляем ему сообщение Вы вошли в чат
                                        DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                        out.writeUTF("Вы вошли в чат.");
                                        continue;
                                    }
                                    DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                    out.writeUTF(currentUser.getUserName() + " теперь в чате.");
                                }
                                while (true) {
                                    String request = in.readUTF();//читаем сообщения от Клиента
                                    System.out.println(currentUser.getUserName() + ": " + request);//Логирование действий пользователя на Сервере
                                    for (User user : users) {
                                        if (users.indexOf(currentUser) == users.indexOf(user))
                                            continue;//Не отправляем сообщение самому пользователю, который отправил это сообщение
                                        DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                        out.writeUTF(currentUser.getUserName() + ": " + request);
                                    }
                                }
                            } catch (IOException e) {
                                users.remove(currentUser);//удаляем Клиента из коллекции Пользователей
                                System.out.println(currentUser.getUserName() + " теперь НЕ в чате.");//Выводим сообщение об имени пользователя, который вышел из чата в консоль чата
                                for (User user : users) {
                                    try {
                                        DataOutputStream out = new DataOutputStream(user.getSocket().getOutputStream());
                                        out.writeUTF(currentUser.getUserName() + " теперь НЕ в чате.");
                                    } catch (IOException ex) {
                                        ex.printStackTrace();
                                    }
                                }
                            }
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

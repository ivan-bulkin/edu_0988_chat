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
                                out.writeUTF("Введите Ваше имя: ");
                                String userName = in.readUTF();//Ожидаем имя от Клиента
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

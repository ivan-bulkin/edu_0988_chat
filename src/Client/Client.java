package Client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("193.168.46.140", 8188);//localhost
            System.out.println("Вы успешно подключёны");
            DataInputStream in = new DataInputStream(socket.getInputStream());//это поток ввода
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());//это поток вывода
            String response = in.readUTF();//принимаем и читаем ответ от Сервера
            System.out.println(response);
            response = in.readUTF();
            System.out.print(response);//Здесь печатаем приглашение о вводе имени(приходит от Сервера)
            Scanner scanner = new Scanner(System.in);//Это нужно, чтобы можно было вводить значения с клавиатуры
            out.writeUTF(scanner.nextLine());//Ожидаем ввод имени
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    String response;
                    while (true) {//Бесконечно ожидаем сообщения от сервера
                        try {
                            response = in.readUTF();//принимаем и читаем ответ от Сервера
                            System.out.println(response);//
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
            thread.start();
            while (true) {
                String consoleText = scanner.nextLine();//Ждём сообщение от пользователя
                out.writeUTF(consoleText);//Отправялем сообщение на сервер
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

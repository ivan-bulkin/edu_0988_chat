package Server;

import java.net.Socket;

public class User {
    private Socket socket;
    private String userName;

    public User(Socket socket) {
        this.socket = socket;
    }

    public Socket getSocket() {
        return socket;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}

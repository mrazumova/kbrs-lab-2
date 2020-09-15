package by.bsu.famcs.notepad.server;

import java.net.ServerSocket;
import java.net.Socket;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Starting server...");
            ServerSocket serverSocket = new ServerSocket(8080);
            System.out.println("Listening...");
            while (true){
                Socket socket = serverSocket.accept();
                System.out.println("Connected " + socket.getInetAddress());

                Connection connection = new Connection(socket);
                connection.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

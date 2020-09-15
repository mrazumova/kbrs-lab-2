package by.bsu.famcs.notepad.client;

import by.bsu.famcs.notepad.client.service.AppProperties;
import by.bsu.famcs.notepad.client.security.RSA;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class ClientSocket {

    private static ObjectInputStream inputStream;

    private static ObjectOutputStream outputStream;

    private static Socket socket;

    public static void login(String login, String password) throws IOException, ClassNotFoundException, AuthenticationException {
        socket = new Socket(AppProperties.getHost(), AppProperties.getPort());
        inputStream = new ObjectInputStream(socket.getInputStream());
        outputStream = new ObjectOutputStream(socket.getOutputStream());

        RSA.generatePublicKey();
        outputStream.writeObject(RSA.e);
        outputStream.writeObject(RSA.n);
        outputStream.flush();

        byte[] encodedSessionKey = (byte[]) inputStream.readObject();
        byte[] decodedSessionKey = RSA.decrypt(encodedSessionKey);

        outputStream.writeObject(login);
        outputStream.writeObject(password);
        outputStream.flush();

        boolean isAuthenticated = (boolean) inputStream.readObject();
        if (!isAuthenticated) {
            close();
            throw new AuthenticationException("Invalid username or password.");
        }
    }

    public static void close() throws IOException {
        socket.close();
        inputStream.close();
        outputStream.close();
    }

}

package by.bsu.famcs.notepad.client;

import by.bsu.famcs.notepad.client.security.RSA;
import by.bsu.famcs.notepad.client.service.AppProperties;

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

    public static String openFile(String path) throws IOException, ClassNotFoundException {
        outputStream.writeObject("OPEN");
        outputStream.writeObject(path);
        outputStream.flush();

        return (String) inputStream.readObject();
    }

    public static String saveFile(String path, String text) throws IOException, ClassNotFoundException {
        outputStream.writeObject("SAVE");
        outputStream.writeObject(path);
        outputStream.writeObject(text);
        outputStream.flush();

        return (String) inputStream.readObject();
    }

    public static String createFile(String path) throws IOException, ClassNotFoundException {
        outputStream.writeObject("CREATE");
        outputStream.writeObject(path);
        outputStream.flush();

        return (String) inputStream.readObject();
    }

    public static String deleteFile(String path) throws IOException, ClassNotFoundException {
        outputStream.writeObject("DELETE");
        outputStream.writeObject(path);
        outputStream.flush();

        return (String) inputStream.readObject();
    }

    public static void close() throws IOException {
        socket.close();
        inputStream.close();
        outputStream.close();
    }

}

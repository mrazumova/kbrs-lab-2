package by.bsu.famcs.notepad.client;

import by.bsu.famcs.notepad.client.security.RSA;
import by.bsu.famcs.notepad.client.service.AppProperties;
import by.bsu.famcs.notepad.serpent.Serpent;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;


public class ClientSocket {

    private static RSA rsa = new RSA();

    private static ObjectInputStream inputStream;

    private static ObjectOutputStream outputStream;

    private static Socket socket;

    private static Serpent serpent;

    public static void login(String login, String password) throws IOException, ClassNotFoundException, AuthenticationException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        socket = new Socket(AppProperties.getHost(), AppProperties.getPort());
        inputStream = new ObjectInputStream(socket.getInputStream());
        outputStream = new ObjectOutputStream(socket.getOutputStream());

        rsa.generatePublicKey();
        outputStream.writeObject(rsa.e);
        outputStream.writeObject(rsa.n);
        outputStream.flush();

        byte[] encodedSessionKey = (byte[]) inputStream.readObject();
        byte[] decodedSessionKey = rsa.decrypt(encodedSessionKey);

        serpent = new Serpent();
        serpent.setKey(decodedSessionKey);

        String encryptedLogin = serpent.encrypt(login);
        String encryptedPassword = serpent.encrypt(password);

        outputStream.writeObject(encryptedLogin);
        outputStream.writeObject(encryptedPassword);
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

        String encodedText = (String) inputStream.readObject();
        return serpent.decrypt(encodedText);
    }

    public static String saveFile(String path, String text) throws IOException, ClassNotFoundException {
        outputStream.writeObject("SAVE");
        outputStream.writeObject(path);

        String encodedText = serpent.encrypt(text);
        outputStream.writeObject(encodedText);
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

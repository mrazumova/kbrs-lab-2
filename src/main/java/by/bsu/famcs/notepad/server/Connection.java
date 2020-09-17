package by.bsu.famcs.notepad.server;

import by.bsu.famcs.notepad.serpent.Serpent;
import by.bsu.famcs.notepad.server.auth.AuthenticationService;
import by.bsu.famcs.notepad.server.security.RSA;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;

public class Connection extends Thread {

    private final int KEY_LENGTH = 128;

    private final ObjectOutputStream outputStream;

    private final ObjectInputStream inputStream;

    private final String address;

    private Serpent serpent;

    private BigInteger e, n;

    public Connection(Socket socket) throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        address = socket.getInetAddress().toString();
        serpent = new Serpent();
    }

    @Override
    public synchronized void start() {
        try {
            e = (BigInteger) inputStream.readObject();
            n = ((BigInteger) inputStream.readObject());
            System.out.println("[" + address + "] RSA public key e: " + e);
            System.out.println("[" + address + "] RSA public key n: " + n);

            byte[] sessionKey = generateSessionKey();
            System.out.println("[" + address + "] SessionKey size: " + sessionKey.length);

            byte[] encodedSessionKey = RSA.encrypt(sessionKey, e, n);
            outputStream.writeObject(encodedSessionKey);

            serpent.setKey(sessionKey);

            authenticate();

            while (true) {
                String action = (String) inputStream.readObject();
                String path = (String) inputStream.readObject();
                String result;
                switch (action) {
                    case "OPEN":
                        result = getTextFromFile(new File(path));
                        String encryptedResult = serpent.encrypt(result);
                        outputStream.writeObject(encryptedResult);
                        outputStream.flush();
                        break;
                    case "SAVE":
                        String encodedText = (String) inputStream.readObject();
                        String decodedText = serpent.decrypt(encodedText);
                        result = saveFile(new File(path), decodedText);
                        outputStream.writeObject(result);
                        outputStream.flush();
                        break;
                    case "CREATE":
                        result = createFile(new File(path));
                        outputStream.writeObject(result);
                        outputStream.flush();
                        break;
                    case "DELETE":
                        result = deleteFile(new File(path));
                        outputStream.writeObject(result);
                        outputStream.flush();
                        break;
                }
            }
        } catch (ClassNotFoundException | IOException e) {
            //e.printStackTrace();
        }
    }

    private String getTextFromFile(File file) {
        try {
            StringBuilder builder = new StringBuilder();
            Scanner scanner = new Scanner(file);
            while (scanner.hasNext())
                builder.append(scanner.nextLine());
            scanner.close();
            return builder.toString();
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String saveFile(File file, String text) {
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(text);
            writer.close();
            return "";
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String createFile(File file) {
        try {
            file.createNewFile();
            return "";
        } catch (IOException e) {
            return e.getMessage();
        }
    }

    private String deleteFile(File file) {
        if (file.delete())
            return "";
        return "Couldn't delete this file.";
    }

    private byte[] generateSessionKey() {
        SecureRandom random = new SecureRandom();
        byte[] generated = new byte[KEY_LENGTH / 2];

        random.nextBytes(generated);
        StringBuilder builder = new StringBuilder();
        for (int i : generated) {
            String hex = Integer.toHexString(i & 0xff);
            if (hex.length() % 2 == 1)
                hex = "0" + hex;
            builder.append(hex);
        }

        return builder.toString().getBytes();
    }

    private void authenticate() throws IOException, ClassNotFoundException {
        String encryptedLogin = (String) inputStream.readObject();
        String encryptedPassword = (String) inputStream.readObject();

        String decryptedLogin = serpent.decrypt(encryptedLogin);
        String decryptedPassword = serpent.decrypt(encryptedPassword);

        boolean isAuthenticated = AuthenticationService.authenticate(decryptedLogin, decryptedPassword);

        outputStream.writeObject(isAuthenticated);
        if (isAuthenticated)
            System.out.println("[" + address + "] User " + decryptedLogin + " logged in successfully");
        else
            System.out.println("[" + address + "] Authentication failed. Reconnecting... " + address);
    }
}

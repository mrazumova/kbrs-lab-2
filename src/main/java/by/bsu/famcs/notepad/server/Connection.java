package by.bsu.famcs.notepad.server;

import by.bsu.famcs.notepad.server.auth.AuthenticationService;
import by.bsu.famcs.notepad.server.security.RSA;

import java.io.*;
import java.math.BigInteger;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.Scanner;

public class Connection extends Thread {

    private final ObjectOutputStream outputStream;
    private final ObjectInputStream inputStream;
    private final String address;
    private BigInteger e, n;

    public Connection(Socket socket) throws IOException {
        outputStream = new ObjectOutputStream(socket.getOutputStream());
        inputStream = new ObjectInputStream(socket.getInputStream());
        address = socket.getInetAddress().toString();
    }

    @Override
    public synchronized void start() {
        try {
            e = (BigInteger) inputStream.readObject();
            n = ((BigInteger) inputStream.readObject());
            System.out.println("RSA public key e: " + e);
            System.out.println("RSA public key n: " + n);

            byte[] sessionKey = generateSessionKey();
            System.out.println("SessionKey size: " + sessionKey.length);

            byte[] encodedSessionKey = RSA.encrypt(sessionKey, e, n);
            outputStream.writeObject(encodedSessionKey);

            authenticate();

            while (true) {
                String action = (String) inputStream.readObject();
                String path = (String) inputStream.readObject();
                String result;
                switch (action) {
                    case "OPEN":
                        result = getTextFromFile(new File(path));
                        outputStream.writeObject(result);
                        outputStream.flush();
                        break;
                    case "SAVE":
                        String text = (String) inputStream.readObject();
                        result = saveFile(new File(path), text);
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
            e.printStackTrace();
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
        byte[] generated = new byte[64]; //key length = 128

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
        String login = (String) inputStream.readObject();
        String password = (String) inputStream.readObject();

        boolean isAuthenticated = AuthenticationService.authenticate(login, password);

        outputStream.writeObject(isAuthenticated);
        if (isAuthenticated)
            System.out.println("User " + login + " logged in successfully");
        else
            System.out.println("Authentication failed. Reconnecting... " + address);
    }
}

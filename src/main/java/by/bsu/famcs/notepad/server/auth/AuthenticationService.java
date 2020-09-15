package by.bsu.famcs.notepad.server.auth;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AuthenticationService {

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("login.properties"));
        } catch (IOException e) {
            System.out.println("ERROR: Cannot read login properties");
        }
    }

    public static boolean authenticate(String login, String password) {
        String storedPassword = getStoredPassword(login);
        if (storedPassword == null || !storedPassword.equals(password))
            return false;
        return true;
    }

    private static String getStoredPassword(String login) {
        return properties.getProperty(String.format("%s.password", login));
    }
}

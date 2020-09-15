package by.bsu.famcs.notepad.client.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class AppProperties {

    private static Properties properties = new Properties();

    static {
        try {
            properties.load(new FileInputStream("app.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getHost() {
        return properties.getProperty("host");
    }

    public static int getPort() {
        return Integer.parseInt(properties.getProperty("port"));
    }
}

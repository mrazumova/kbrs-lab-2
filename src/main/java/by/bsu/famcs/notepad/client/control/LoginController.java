package by.bsu.famcs.notepad.client.control;

import by.bsu.famcs.notepad.client.ClientSocket;
import by.bsu.famcs.notepad.client.service.Loader;
import by.bsu.famcs.notepad.client.service.Notification;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

import javax.naming.AuthenticationException;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML
    private JFXTextField usernameField;

    @FXML
    private JFXPasswordField passwordField;

    private Loader loader;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loader = new Loader();
    }

    @FXML
    private void signIn(MouseEvent event) {
        String username = usernameField.getText().trim();
        String password = passwordField.getText().trim();
        if (username.isEmpty() && password.isEmpty()) {
            new Notification("Error!", "Fields cannot be empty.");
        } else {
            try {
                ClientSocket.login(username, password);
                loader.initPage(event, "/view/home.fxml");
            } catch (ClassNotFoundException | IOException e) {
                new Notification("Error!", e.getMessage());
            } catch (AuthenticationException e) {
                new Notification("Error!", "Invalid username or password.");
            }
        }

    }

    @FXML
    private void closeWindow(MouseEvent event) {
        try {
            ClientSocket.close();
        } catch (IOException e) {
        }
        System.exit(0);
    }
}

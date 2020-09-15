package by.bsu.famcs.notepad.client.view.control;

import by.bsu.famcs.notepad.client.view.service.Loader;
import by.bsu.famcs.notepad.client.view.service.Notification;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.MouseEvent;

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
            loader.initPage(event, "/view/home.fxml");
            /*try {
                //User.login(username, password);
                loader.initPage(event, "/view/home.fxml");
            } catch (IOException e) {
                new Notification("Error!", "Invalid username or password.");
            }*/
        }
    }

    @FXML
    private void closeWindow(MouseEvent event) {
        System.exit(0);
    }
}

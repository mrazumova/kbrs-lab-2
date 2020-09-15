package by.bsu.famcs.notepad.client.control;

import by.bsu.famcs.notepad.client.ClientSocket;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private TextField pathToFile;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void openFile(){
        String path = pathToFile.getText().trim();
    }

    @FXML
    private void saveFile(){

    }

    @FXML
    private void deleteFile(){

    }

    @FXML
    private void closeWindow(MouseEvent event) {
        try {
            ClientSocket.close();
        } catch (IOException e) {}
        System.exit(0);
    }
}

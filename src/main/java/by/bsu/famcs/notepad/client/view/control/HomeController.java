package by.bsu.famcs.notepad.client.view.control;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

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
        System.exit(0);
    }
}

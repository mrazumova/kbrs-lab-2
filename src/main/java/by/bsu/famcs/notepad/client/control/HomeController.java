package by.bsu.famcs.notepad.client.control;

import by.bsu.famcs.notepad.client.ClientSocket;
import by.bsu.famcs.notepad.client.service.Notification;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class HomeController implements Initializable {

    @FXML
    private TextField pathToFile;

    @FXML
    private TextArea textArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    private void openFile(){
        String path = pathToFile.getText().trim();
        try {
            String text = ClientSocket.openFile(path);
            textArea.setDisable(false);
            textArea.setText(text);
        } catch (IOException | ClassNotFoundException e){
            new Notification("ERROR: Open file", e.getMessage());
        }
    }

    @FXML
    private void saveFile(){
        String path = pathToFile.getText().trim();
        try{
            String result = ClientSocket.saveFile(path, textArea.getText());
            if (result.isEmpty())
                new Notification("Save file", "File was saved successfully.");
            else
                new Notification("ERROR: Save file", result);
        } catch (IOException | ClassNotFoundException e){
            new Notification("ERROR: Save file", e.getMessage());
        }
    }

    @FXML
    private void createFile(){
        String path = pathToFile.getText().trim();
        try{
            ClientSocket.createFile(path);
            textArea.setText("");
            textArea.setDisable(false);
            new Notification("Create file", "File was created successfully.");
        } catch (IOException | ClassNotFoundException e){
            new Notification("ERROR: Create file", e.getMessage());
        }
    }

    @FXML
    private void deleteFile(){
        String path = pathToFile.getText().trim();
        try{
            ClientSocket.deleteFile(path);
            textArea.setText("");
            new Notification("Delete file", "File was deleted successfully.");
            textArea.setDisable(true);
        } catch (IOException | ClassNotFoundException e) {
            new Notification("ERROR: Delete file", e.getMessage());
        }
    }

    @FXML
    private void closeWindow(MouseEvent event) {
        try {
            ClientSocket.close();
        } catch (IOException e) {}
        System.exit(0);
    }
}

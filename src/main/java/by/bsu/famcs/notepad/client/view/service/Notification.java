package by.bsu.famcs.notepad.client.view.service;

import javafx.geometry.Pos;
import javafx.util.Duration;
import org.controlsfx.control.Notifications;

public class Notification {

    public Notification(String title, String text) {
        Notifications nf = makeNotification(title, text);
        nf.showError();
    }

    private Notifications makeNotification(String title, String text) {
        Notifications builder = Notifications.create()
                .title(title)
                .text(text)
                .graphic(null)
                .hideAfter(Duration.seconds(3))
                .position(Pos.BOTTOM_RIGHT);
        return builder;
    }
}

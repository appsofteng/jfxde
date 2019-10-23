package dev.jfxde.ui;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.Pane;

public final class AlertBuilder {

    private Alert alert;
    private Pane owner;
    private Runnable action = () -> {};

    private AlertBuilder(Pane owner, Alert alert) {
       this.owner = owner;
       this.alert = alert;
    }

    public static AlertBuilder get(Pane owner, AlertType alertType) {
        return new AlertBuilder(owner, new Alert(alertType));
    }

    public AlertBuilder title(String title) {
        alert.setTitle(title);
        return this;
    }

    public AlertBuilder headerText(String text) {
        alert.setHeaderText(text);
        return this;
    }

    public AlertBuilder contentText(String text) {
        alert.setContentText(text);
        return this;
    }

    public AlertBuilder expandableContent(Node content) {
        alert.getDialogPane().setExpandableContent(content);
        return this;
    }

    public AlertBuilder action(Runnable action) {
        this.action = action;
        return this;
    }

    public void show() {

        var dialogPane = alert.getDialogPane();
        double w = Math.max(dialogPane.minWidth(-1), dialogPane.getWidth());

        //This is necessary because the dialog pane layout children method has an error.
        dialogPane.heightProperty().addListener((v,o,n) -> {
            dialogPane.setMinHeight(dialogPane.prefHeight(w));
        });
        dialogPane.expandedProperty().addListener((v,n,o) -> {
            dialogPane.setMinHeight(dialogPane.prefHeight(w));
        });

        var dialog = new InternalDialog(owner);
        dialog.setTitle(alert.getTitle());
        dialog.setUseComputedSize();

        final Button ok = (Button) dialogPane.lookupButton(ButtonType.OK);
        ok.setOnAction(e -> {
            dialog.close();
            action.run();
        });

        final Button cancel = (Button) dialogPane.lookupButton(ButtonType.CANCEL);
        cancel.setOnAction(e -> {
            dialog.close();
        });

        dialog.setContent(dialogPane).show();
    }
}

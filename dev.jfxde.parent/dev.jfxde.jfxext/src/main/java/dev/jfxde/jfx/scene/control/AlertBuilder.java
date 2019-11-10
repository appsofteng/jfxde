package dev.jfxde.jfx.scene.control;

import java.util.HashMap;
import java.util.Map;

import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;

public final class AlertBuilder {

    private Alert alert;
    private InternalDialog dialog;
    private Map<ButtonType, Runnable> actions = new HashMap<>();

    private AlertBuilder(Node owner, Alert alert) {
       dialog = new InternalDialog(owner, Modality.WINDOW_MODAL);
       this.alert = alert;
    }

    public static AlertBuilder get(Node owner, AlertType alertType) {
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

    public AlertBuilder expandableTextArea(String content) {
        TextArea area = new TextArea(content);
        area.setEditable(false);
        alert.getDialogPane().setExpandableContent(area);
        return this;
    }

    public AlertBuilder expandableContent(Node content) {
        alert.getDialogPane().setExpandableContent(content);
        return this;
    }

    public AlertBuilder ok(Runnable action) {
        this.actions.put(ButtonType.OK, action);
        return this;
    }

    public AlertBuilder cancel(Runnable action) {
        this.actions.put(ButtonType.CANCEL, action);
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

        dialog.setTitle(alert.getTitle());
        dialog.setUseComputedSize();

        for (var buttonType: dialogPane.getButtonTypes()) {
            Button button = (Button) dialogPane.lookupButton(buttonType);

            button.setOnAction(e -> {
                dialog.close();
                var action = actions.get(buttonType);
                if (action != null) {
                    action.run();
                }
            });
        }

        dialog.setContent(dialogPane).show();
    }
}

package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SetBox;
import dev.jfxde.ui.InternalDialog;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import picocli.CommandLine.Command;

@Command(name = "/set")
public class SetCommand extends BaseCommand {

    public SetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        Platform.runLater(() -> {

            InternalDialog d = InternalDialog.create(commandProcessor.getSession().getContent(), true);
            SetBox setBox = new SetBox(commandProcessor.getSession().getContext(), commandProcessor.getSession().loadSettings());
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(setBox);
            ButtonType okButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("ok"), ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("cancel"),
                    ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(okButtonType, cancelButtonType);
            final Button btOk = (Button) dialogPane.lookupButton(okButtonType);
            btOk.setOnAction(e -> {
                d.close();
                commandProcessor.getSession().setSettings(setBox.getSettings());
            });
            final Button cancel = (Button) dialogPane.lookupButton(cancelButtonType);
            cancel.setOnAction(e -> d.close());

            d.show(dialogPane);
        });
    }
}

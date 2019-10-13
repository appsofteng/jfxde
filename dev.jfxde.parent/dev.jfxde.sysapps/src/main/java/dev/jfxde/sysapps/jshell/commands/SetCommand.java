package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SetBox;
import dev.jfxde.ui.InternalDialog;
import javafx.application.Platform;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
import picocli.CommandLine.Command;

@Command(name = "/set")
public class SetCommand extends BaseCommand {

    public SetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        Platform.runLater(() -> {

//            InternalDialog d = InternalDialog.create(commandProcessor.getSession().getContent(), true);
//            d.show();

//            Dialog<ButtonType> dialog = new Dialog<>();
//            dialog.initOwner(commandProcessor.getSession().getWindow());
//            dialog.initModality(Modality.APPLICATION_MODAL);
//            dialog.initStyle(StageStyle.UTILITY);
//
//            SetBox setBox = new SetBox(commandProcessor.getSession().getContext(), commandProcessor.getSession().loadSettings());
//            dialog.getDialogPane().setContent(setBox);
//            ButtonType okButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("ok"), ButtonData.OK_DONE);
//            ButtonType cancelButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("cancel"),
//                    ButtonData.CANCEL_CLOSE);
//            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
//            dialog.showAndWait().filter(response -> response == okButtonType)
//                    .ifPresent(b -> commandProcessor.getSession().setSettings(setBox.getSettings()));
        });
    }
}

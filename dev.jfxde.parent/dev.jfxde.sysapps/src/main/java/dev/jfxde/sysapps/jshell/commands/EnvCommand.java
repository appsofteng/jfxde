package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.Env;
import dev.jfxde.sysapps.jshell.EnvBox;
import dev.jfxde.sysapps.jshell.Session;
import dev.jfxde.ui.InternalDialog;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.DialogPane;
import javafx.stage.Modality;
import picocli.CommandLine.Command;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    private static final String ENVS_FILE_NAME = "envs.json";

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        Platform.runLater(() -> {

            InternalDialog dialog = new InternalDialog(commandProcessor.getSession().getContent(), Modality.WINDOW_MODAL);
            dialog.setTitle(commandProcessor.getSession().getContext().rc().getString("environment"));
            EnvBox envBox = new EnvBox(commandProcessor.getSession().getContext(), getEnvs());
            DialogPane dialogPane = new DialogPane();
            dialogPane.setContent(envBox);
            ButtonType resetdButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("reset"), ButtonData.OK_DONE);
            ButtonType reloadButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("reload"), ButtonData.APPLY);
            ButtonType cancelButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("cancel"),
                    ButtonData.CANCEL_CLOSE);
            dialogPane.getButtonTypes().addAll(resetdButtonType, reloadButtonType, cancelButtonType);
            final Button reset = (Button) dialogPane.lookupButton(resetdButtonType);
            reset.setOnAction(e -> {
                dialog.close();
                resetEnvs(envBox.getEnv(), envBox.getEnvs());
            });
            final Button reload = (Button) dialogPane.lookupButton(reloadButtonType);
            reload.setOnAction(e -> {
                dialog.close();
                reloadEnvs(envBox.getEnv(), envBox.getEnvs());
            });
            final Button cancel = (Button) dialogPane.lookupButton(cancelButtonType);
            cancel.setOnAction(e -> dialog.close());

            dialog.setContent(dialogPane).show();

        });
    }

    private ObservableList<Env> getEnvs() {
        ObservableList<Env> envs = FXCollections.observableArrayList();
        envs.add(commandProcessor.getSession().loadEnv());

        List<Env> envsList = commandProcessor.getSession().getContext().dc().fromJson(ENVS_FILE_NAME,
                new ArrayList<Env>(){}.getClass().getGenericSuperclass(), new ArrayList<>());
        envs.addAll(envsList);

        return envs;
    }

    private void resetEnvs(Env env, ObservableList<Env> envs) {

        commandProcessor.getSession().resetEnv(env);
        saveEnvs(env, envs);

    }

    private void reloadEnvs(Env env, ObservableList<Env> envs) {
        commandProcessor.getSession().reloadEnv(env);
        saveEnvs(env, envs);
    }

    private void saveEnvs(Env env, ObservableList<Env> envs) {
        envs.remove(env);
        commandProcessor.getSession().getContext().dc().toJson(envs, ENVS_FILE_NAME);
    }
}

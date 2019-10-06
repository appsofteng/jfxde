package dev.jfxde.sysapps.jshell.commands;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.reflect.TypeToken;

import dev.jfxde.logic.JsonUtils;
import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.Env;
import dev.jfxde.sysapps.jshell.EnvBox;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import javafx.stage.StageStyle;
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
            Dialog<ButtonType> dialog = new Dialog<>();
            dialog.initOwner(commandProcessor.getSession().getWindow());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.initStyle(StageStyle.UTILITY);

            EnvBox envBox = new EnvBox(commandProcessor.getSession().getContext(), getEnvs());
            dialog.getDialogPane().setContent(envBox);
            ButtonType okButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("ok"), ButtonData.OK_DONE);
            ButtonType cancelButtonType = new ButtonType(commandProcessor.getSession().getContext().rc().getString("cancel"),
                    ButtonData.CANCEL_CLOSE);
            dialog.getDialogPane().getButtonTypes().addAll(okButtonType, cancelButtonType);
            dialog.showAndWait().filter(response -> response == okButtonType)
                    .ifPresent(b -> saveEnvs(envBox.getEnv(), envBox.getEnvs()));
        });

    }

    private ObservableList<Env> getEnvs() {
        ObservableList<Env> envs = FXCollections.observableArrayList();
        envs.add(commandProcessor.getSession().getEnv());

        Type listType = new TypeToken<ArrayList<Env>>(){}.getType();
        List<Env> envsList = JsonUtils.fromJson(commandProcessor.getSession().getContext().fc().getAppDataDir().resolve(ENVS_FILE_NAME), listType, new ArrayList<>());
        envs.addAll(envsList);

        return envs;
    }

    private void saveEnvs(Env env, ObservableList<Env> envs) {
        commandProcessor.getSession().setEnv(env);
        envs.remove(env);
        JsonUtils.toJson(env, commandProcessor.getSession().getContext().fc().getAppDataDir().resolve(ENVS_FILE_NAME));
    }
}

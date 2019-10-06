package dev.jfxde.sysapps.jshell;

import org.controlsfx.control.ListSelectionView;

import dev.jfxde.api.AppContext;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class EnvBox extends VBox {

    private AppContext context;
    private ObservableList<String> sourceModules = FXCollections.observableArrayList();
    private ObservableList<Env> envs;
    private Env env;

    private ComboBox<Env> envCombo;

    public EnvBox(AppContext context, ObservableList<Env> envs) {
        this.context = context;
        this.envs = envs;
        this.env = envs.get(0);
        FXCollections.sort(envs);
        setGraphics();
        setBehavior();
    }

    private void setGraphics() {
        envCombo = new ComboBox<>(envs);
        envCombo.getSelectionModel().select(env);
        envCombo.setEditable(true);
        Button addEnv = new Button("+");
        Button removeEnv = new Button("-");

        HBox envBox = new HBox(envCombo, addEnv, removeEnv);
        HBox.setMargin(addEnv, new Insets(0, 5, 0, 5));

        Label classpath = new Label(context.rc().getString("classpath"));
        ListView<String> classpathView = new ListView<String>(env.getClassPath());
        classpathView.setPrefHeight(100);

        Label modulepath = new Label(context.rc().getString("modulepath"));
        ListView<String> modulepathView = new ListView<String>(env.getModulePath());
        modulepathView.setPrefHeight(100);

        ListSelectionView<String> addModuleView = new ListSelectionView<String>();
        addModuleView.setSourceHeader(new Label(context.rc().getString("availableModules")));
        addModuleView.setTargetHeader(new Label(context.rc().getString("selectedModules")));
        addModuleView.setSourceItems(sourceModules);
        addModuleView.setTargetItems(env.getAddModules());
        addModuleView.setPrefHeight(100);

        Label addExports = new Label(context.rc().getString("exports"));
        ListView<ExportItem> exportView = new ListView<>(env.getAddExports());
        exportView.setPrefHeight(100);

        getChildren().addAll(envBox, classpath, classpathView, modulepath, modulepathView, addModuleView, addExports, exportView);
    }

    private void setBehavior() {


    }

    public Env getEnv() {
        return env;
    }

    public ObservableList<Env> getEnvs() {
        return envs;
    }
}

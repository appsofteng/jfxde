package dev.jfxde.sysapps.jshell;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.net.URI;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.controlsfx.control.ListSelectionView;

import dev.jfxde.api.AppContext;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;

public class EnvBox extends VBox {

    private AppContext context;
    private ObservableList<String> sourceModules = FXCollections.observableArrayList();
    private ObservableList<String> targetModules;
    private ObservableList<Env> envs;
    private Env env;

    private ComboBox<Env> envCombo;
    private ListView<String> classpathView;
    private ListView<String> modulepathView;
    private ListSelectionView<String> addModuleView;
    private ListView<ExportItem> exportView;
    private Button addEnv;
    private Button removeEnv;
    private Map<String, URI> moduleLocations = new HashMap<>();

    public EnvBox(AppContext context, ObservableList<Env> envs) {
        this.context = context;
        this.envs = envs;
        this.env = envs.get(0);
        FXCollections.sort(envs);
        setGraphics();
        setEnv();
        setBehavior();
        setClassPathContextMenu();
        setModulePathContextMenu();
    }

    private void setGraphics() {
        envCombo = new ComboBox<>(envs);
        envCombo.getSelectionModel().select(env);
        envCombo.setEditable(true);
        addEnv = new Button("+");
        removeEnv = new Button("-");

        HBox envBox = new HBox(envCombo, addEnv, removeEnv);
        HBox.setMargin(addEnv, new Insets(0, 5, 0, 5));

        Label classpath = new Label(context.rc().getString("classpath"));
        classpathView = new ListView<>();
        classpathView.setPrefHeight(100);
        classpathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label modulepath = new Label(context.rc().getString("modulepath"));
        modulepathView = new ListView<>();
        modulepathView.setPrefHeight(100);
        modulepathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addModuleView = new ListSelectionView<>();
        addModuleView.setSourceHeader(new Label(context.rc().getString("availableModules")));
        addModuleView.setTargetHeader(new Label(context.rc().getString("selectedModules")));
        addModuleView.setSourceItems(sourceModules);
        addModuleView.setPrefHeight(100);

        Label addExports = new Label(context.rc().getString("exports"));
        exportView = new ListView<>();
        exportView.setPrefHeight(100);
        exportView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        getChildren().addAll(envBox, classpath, classpathView, modulepath, modulepathView, addModuleView, addExports, exportView);
    }

    private void setBehavior() {

        envCombo.setConverter(new StringConverter<Env>() {

            @Override
            public String toString(Env object) {
                if (object == null)
                    return null;
                return object.toString();
            }

            @Override
            public Env fromString(String string) {
                return env;
            }
        });

        envCombo.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                env = n;
                setEnv();
            }
        });

        envCombo.getEditor().focusedProperty().addListener((v, o, n) -> {
            if (!n) {
                if (envCombo.getEditor().getText() == null || envCombo.getEditor().getText().isBlank()) {
                    envCombo.getEditor().setText(env.getName());
                } else {
                    env.setName(envCombo.getEditor().getText());
                    FXCollections.sort(envs);
                }
            }
        });

        addEnv.setOnAction(e -> addEnv());

        removeEnv.setOnAction(e -> {
            envs.remove(env);
            if (envs.isEmpty()) {
                addEnv();
            } else {
                env = envs.get(0);
                setEnv();
                envCombo.getSelectionModel().select(env);
            }
        });
    }

    private void setClassPathContextMenu() {
        MenuItem addFiles = new MenuItem(context.rc().getString("addFiles"));
        addFiles.setOnAction(e -> {
            classpathView.getItems().addAll(getFiles(env.getClassPath()));
        });

        MenuItem addDirectory = new MenuItem(context.rc().getString("addDirectory"));
        addDirectory.setOnAction(e -> {
            classpathView.getItems().addAll(getDirectory(env.getClassPath()));
        });

        MenuItem removeSelection = new MenuItem(context.rc().getString("removeSelection"));
        removeSelection.disableProperty().bind(classpathView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            classpathView.getItems().removeAll(classpathView.getSelectionModel().getSelectedItems());
            ;
        });

        ContextMenu menu = new ContextMenu(addFiles, addDirectory, removeSelection);
        classpathView.setContextMenu(menu);
    }

    private void setModulePathContextMenu() {

        MenuItem addDirectory = new MenuItem(context.rc().getString("addDirectory"));
        addDirectory.setOnAction(e -> {
            modulepathView.getItems().addAll(getDirectory(env.getModulePath()));
        });

        MenuItem removeSelection = new MenuItem(context.rc().getString("removeSelection"));
        removeSelection.disableProperty().bind(modulepathView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            modulepathView.getItems().removeAll(modulepathView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(addDirectory, removeSelection);
        modulepathView.setContextMenu(menu);
    }

    private List<String> getFiles(List<String> current) {
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(getScene().getWindow());

        List<String> paths = files == null ? List.of()
                : files.stream().map(f -> f.toString()).filter(p -> !current.contains(p)).collect(Collectors.toList());

        return paths;
    }

    private List<String> getDirectory(List<String> current) {
        DirectoryChooser chooser = new DirectoryChooser();
        File dir = chooser.showDialog(getScene().getWindow());

        List<String> paths = Stream.of(dir).filter(d -> d != null).map(d -> d.toString()).filter(p -> !current.contains(p))
                .collect(Collectors.toList());

        return paths;
    }

    private Map<String, URI> getModules() {
        List<Path> paths = env.getModulePath().stream().map(p -> Paths.get(p)).collect(Collectors.toList());
        ModuleFinder mf = ModuleFinder.of(paths.toArray(new Path[] {}));

        return mf.findAll().stream().collect(Collectors.toMap(r -> r.descriptor().name(), r -> r.location().orElse(null)));
    }

    private void setEnv() {
        classpathView.setItems(FXCollections.observableList(env.getClassPath()));
        modulepathView.setItems(FXCollections.observableList(env.getModulePath()));
        targetModules = FXCollections.observableList(env.getAddModules());
        addModuleView.setTargetItems(targetModules);
        exportView.setItems(FXCollections.observableList(env.getAddExports()));

        moduleLocations = getModules();
        sourceModules.setAll(moduleLocations.keySet().stream().sorted().collect(Collectors.toList()));

        modulepathView.getItems().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    moduleLocations = getModules();
                    sourceModules.setAll(moduleLocations.keySet().stream().sorted().collect(Collectors.toList()));
                }
            }
        });


        targetModules.addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    env.getModuleLocations().clear();
                    env.getModuleLocations().addAll(
                            addModuleView.getTargetItems().stream().map(s -> new File(moduleLocations.get(s)).toString()).collect(Collectors.toList()));
                }
            }
        });
    }

    private void addEnv() {
        env = new Env(context.rc().getString("new"));
        envs.add(env);
        setEnv();
        envCombo.getSelectionModel().select(env);
        FXCollections.sort(envs);
    }

    public Env getEnv() {
        return env;
    }

    public ObservableList<Env> getEnvs() {
        return envs;
    }
}

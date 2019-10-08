package dev.jfxde.sysapps.jshell;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.AutoCompleteTextFieldTableCell;
import io.vavr.control.Try;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class EnvBox extends VBox {

    private AppContext context;
    private ObservableList<String> modulePathModules = FXCollections.observableArrayList();
    private Map<String, ModuleReference> modulePathModuleReferences = new HashMap<>();
    private Map<String, ModuleReference> systemModuleReferences;
    private ObservableList<String> exportModules = FXCollections.observableArrayList();
    private Map<String, ModuleReference> exportModuleReferences;
    private ObservableList<Env> envs;
    private Env env;

    private ComboBox<Env> envCombo;
    private ListView<String> classpathView;
    private ListView<String> modulepathView;
    private ListView<String> addModuleView;
    private TableView<ExportItem> exportView;
    private Button addEnv;
    private Button removeEnv;

    public EnvBox(AppContext context, ObservableList<Env> envs) {
        this.context = context;
        this.envs = envs;
        this.env = envs.get(0);
        FXCollections.sort(envs);
        systemModuleReferences = ModuleFinder.ofSystem().findAll().stream().filter(r -> !r.descriptor().packages().isEmpty())
                .collect(Collectors.toMap(r -> r.descriptor().name(), r -> r));
        setGraphics();
        setEnv();
        setBehavior();
        setClassPathContextMenu();
        setModulePathContextMenu();
        setExportContextMenu();
    }

    @SuppressWarnings("unchecked")
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
        classpathView.setPrefSize(500, 100);
        classpathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label modulepath = new Label(context.rc().getString("modulepath"));
        modulepathView = new ListView<>();
        modulepathView.setPrefHeight(100);
        modulepathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label addModules = new Label(context.rc().getString("addModules"));
        addModuleView = new ListView<>();
        addModuleView.setItems(modulePathModules);
        addModuleView.setPrefHeight(100);
        addModuleView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label addExports = new Label(context.rc().getString("addExports"));
        exportView = new TableView<>();
        exportView.setPrefHeight(100);
        exportView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        exportView.setEditable(true);

        TableColumn<ExportItem, String> sourceColumn = new TableColumn<>();
        sourceColumn.textProperty().bind(context.rc().getStringBinding("sourceModule"));
        sourceColumn.setCellValueFactory(
                f -> Try.of(() -> JavaBeanStringPropertyBuilder.create().bean(f.getValue()).name("sourceModule").build()).getOrNull());
//        sourceColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(availableModules));
        sourceColumn.setCellFactory(f -> {
            TextFieldTableCell<ExportItem, String> cell = new AutoCompleteTextFieldTableCell<>(new DefaultStringConverter(), exportModules) {

                public void commitEdit(String newValue) {

                    if (exportModules.contains(newValue)) {

                        super.commitEdit(newValue);
                        getTableRow().getItem().setPackageName(exportModuleReferences.get(newValue).descriptor().packages().iterator().next());
                        getTableView().refresh();
                    } else {
                        cancelEdit();
                    }
                }
            };

            return cell;
        });

        TableColumn<ExportItem, String> packageColumn = new TableColumn<>();
        packageColumn.textProperty().bind(context.rc().getStringBinding("package"));
        packageColumn.setCellValueFactory(
                f -> Try.of(() -> JavaBeanStringPropertyBuilder.create().bean(f.getValue()).name("packageName").build()).getOrNull());
        packageColumn.setCellFactory(c -> {

            ChoiceBoxTableCell<ExportItem, String> cell = new ChoiceBoxTableCell<>() {
                public void startEdit() {
                    if (getTableRow() != null && getTableRow().getItem() != null && getTableRow().getItem().getSourceModule() != null) {
                        ModuleReference mref = exportModuleReferences.get(getTableRow().getItem().getSourceModule());
                        Set<String> packages = mref == null ? Set.of() : mref.descriptor().packages();
                        getItems().setAll(packages);
                    }
                    super.startEdit();
                }
            };
            return cell;
        });

        TableColumn<ExportItem, String> targetColumn = new TableColumn<>();
        targetColumn.textProperty().bind(context.rc().getStringBinding("targetModule"));
        targetColumn.setCellValueFactory(
                f -> Try.of(() -> JavaBeanStringPropertyBuilder.create().bean(f.getValue()).name("targetModule").build()).getOrNull());
//        targetColumn.setCellFactory(ChoiceBoxTableCell.forTableColumn(availableModules));
        targetColumn.setCellFactory(f -> {
            TextFieldTableCell<ExportItem, String> cell = new AutoCompleteTextFieldTableCell<>(new DefaultStringConverter(), exportModules) {

                public void commitEdit(String newValue) {

                    if (exportModules.contains(newValue)) {
                        super.commitEdit(newValue);
                    } else {
                        cancelEdit();
                    }
                }
            };

            return cell;
        });

        exportView.getColumns().addAll(sourceColumn, packageColumn, targetColumn);

        getChildren().addAll(envBox, classpath, classpathView, modulepath, modulepathView, addModules, addModuleView, addExports, exportView);
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

    private void setExportContextMenu() {

        MenuItem add = new MenuItem(context.rc().getString("addExport"));
        add.disableProperty().bind(javafx.beans.binding.Bindings.size(exportModules).lessThan(2));
        add.setOnAction(e -> {
            exportView.getItems().add(
                    new ExportItem(exportModules.get(0), exportModuleReferences.get(exportModules.get(0)).descriptor().packages().iterator().next(),
                            exportModules.get(2)));
        });

        MenuItem removeSelection = new MenuItem(context.rc().getString("removeSelection"));
        removeSelection.disableProperty().bind(exportView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            exportView.getItems().removeAll(exportView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(add, removeSelection);
        exportView.setContextMenu(menu);
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

    private ListChangeListener<String> addModuleListener = (Change<? extends String> c) -> {

        while (c.next()) {

            if (c.wasAdded() || c.wasRemoved()) {
                env.getAddModules().clear();
                env.getModuleLocations().clear();

                env.getAddModules().addAll(addModuleView.getSelectionModel().getSelectedItems());
                env.getModuleLocations().addAll(
                        addModuleView.getSelectionModel().getSelectedItems().stream()
                                .map(s -> new File(modulePathModuleReferences.get(s).location().orElse(null)).toString())
                                .collect(Collectors.toList()));
            }
        }
    };

    private void setEnv() {
        classpathView.setItems(FXCollections.observableList(env.getClassPath()));
        modulepathView.setItems(FXCollections.observableList(env.getModulePath()));
        exportView.setItems(FXCollections.observableList(env.getAddExports()));

        setModules();

        addModuleView.getSelectionModel().getSelectedItems().removeListener(addModuleListener);
        env.getAddModules().forEach(m -> addModuleView.getSelectionModel().select(m));

        modulepathView.getItems().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    setModules();
                }
            }
        });

        addModuleView.getSelectionModel().getSelectedItems().addListener(addModuleListener);
    }

    private Map<String, ModuleReference> getModulePathModuleReferences() {
        List<Path> paths = env.getModulePath().stream().map(p -> Paths.get(p)).collect(Collectors.toList());
        ModuleFinder mf = ModuleFinder.of(paths.toArray(new Path[] {}));

        return mf.findAll().stream().collect(Collectors.toMap(r -> r.descriptor().name(), r -> r));
    }

    private void setModules() {
        modulePathModuleReferences = getModulePathModuleReferences();
        modulePathModules.setAll(modulePathModuleReferences.keySet().stream().sorted().collect(Collectors.toList()));

        exportModuleReferences = new HashMap<>(modulePathModuleReferences.values().stream().filter(r -> !r.descriptor().packages().isEmpty())
                .collect(Collectors.toMap(r -> r.descriptor().name(), r -> r)));
        exportModuleReferences.putAll(systemModuleReferences);
        exportModules.setAll(exportModuleReferences.keySet());

        FXCollections.sort(exportModules);
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

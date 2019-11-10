package dev.jfxde.sysapps.jshell;

import java.io.File;
import java.lang.module.ModuleFinder;
import java.lang.module.ModuleReference;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.controlsfx.control.ListSelectionView;

import dev.jfxde.api.AppContext;
import dev.jfxde.j.util.LU;
import dev.jfxde.jfx.scene.control.cell.AutoCompleteTextFieldTableCell;
import dev.jfxde.jfx.scene.control.cell.CheckComboBoxTableCell;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.jfx.util.converter.CollectionStringConverter;
import dev.jfxde.ui.FileDialog;
import javafx.beans.binding.Bindings;
import javafx.beans.property.adapter.JavaBeanObjectPropertyBuilder;
import javafx.beans.property.adapter.JavaBeanStringPropertyBuilder;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class EnvBox extends VBox {

    private AppContext context;

    private Map<String, ModuleReference> modulePathModuleReferences = new HashMap<>();
    private Map<String, ModuleReference> systemModuleReferences;
    private ObservableList<String> exportModules = FXCollections.observableArrayList();
    private Map<String, ModuleReference> exportModuleReferences;
    private ObservableList<Env> envs;
    private Env env;

    private ComboBox<Env> envCombo;
    private ListView<String> classpathView;
    private ListView<String> modulepathView;
    private ListSelectionView<String> addModuleView;
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

        Label classpath = new Label(FXResourceBundle.getBundle​(JShellApp.class).getString​("classpath"));
        classpathView = new ListView<>();
        classpathView.setPrefWidth(400);
        classpathView.setPrefHeight(100);
        // Set the sizes or the list view flickers in the dialog pane.
        classpathView.setMinHeight(100);
        classpathView.setMaxHeight(100);
        classpathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        Label modulepath = new Label(FXResourceBundle.getBundle​(JShellApp.class).getString("modulepath"));
        modulepathView = new ListView<>();
        modulepathView.setPrefWidth(400);
        modulepathView.setPrefHeight(100);
        modulepathView.setMinHeight(100);
        modulepathView.setMaxHeight(100);
        modulepathView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        addModuleView = new ListSelectionView<>();
        Label sourceHeader = new Label(FXResourceBundle.getBundle​(JShellApp.class).getString("availableModules"));
        addModuleView.setSourceHeader(sourceHeader);
        Label targetHeader = new Label(FXResourceBundle.getBundle​(JShellApp.class).getString("addModules"));
        addModuleView.setTargetHeader(targetHeader);
        addModuleView.setPrefWidth(400);
        addModuleView.setPrefHeight(100);

        Label addExports = new Label(FXResourceBundle.getBundle​(JShellApp.class).getString("addExports"));
        exportView = new TableView<>();
        exportView.setPrefWidth(400);
        exportView.setPrefHeight(100);
        exportView.setMinHeight(100);
        exportView.setMaxHeight(100);
        exportView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        exportView.setEditable(true);

        TableColumn<ExportItem, String> sourceColumn = new TableColumn<>();
        sourceColumn.textProperty().bind(FXResourceBundle.getBundle​(JShellApp.class).getStringBinding("sourceModule"));
        sourceColumn.setCellValueFactory(
                f -> LU.of((() -> JavaBeanStringPropertyBuilder.create().bean(f.getValue()).name("sourceModule").build())));
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
        packageColumn.textProperty().bind(FXResourceBundle.getBundle​(JShellApp.class).getStringBinding("package"));
        packageColumn.setCellValueFactory(
                f -> LU.of(() -> JavaBeanStringPropertyBuilder.create().bean(f.getValue()).name("packageName").build()));
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

        TableColumn<ExportItem, Collection<String>> targetColumn = new TableColumn<>();
        targetColumn.textProperty().bind(FXResourceBundle.getBundle​(JShellApp.class).getStringBinding("targetModules"));
        targetColumn.setCellValueFactory(
                f -> LU.of(() -> JavaBeanObjectPropertyBuilder.create().bean(f.getValue()).name("targetModules").build()));
        targetColumn.setCellFactory(CheckComboBoxTableCell.forTableColumn(new CollectionStringConverter(), exportModules));

        exportView.getColumns().addAll(sourceColumn, packageColumn, targetColumn);

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

        addModuleView.getTargetItems().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    env.getAddModules().clear();
                    env.getAddModules().addAll(addModuleView.getTargetItems());
                    env.setModuleLocations(env.getAddModules().stream()
                            .filter(m -> modulePathModuleReferences.get(m) != null)
                            .filter(m -> modulePathModuleReferences.get(m).location().isPresent())
                            .map(m -> new File(modulePathModuleReferences.get(m).location().get()).toString()).collect(Collectors.toList()));
                }
            }
        });
    }

    private void setClassPathContextMenu() {
        MenuItem add = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("add"));
        add.setOnAction(e -> {
            new FileDialog(this)
            .setTitle(FXResourceBundle.getBundle​(JShellApp.class).getString("classpath"))
                    .showOpenDialog(paths -> classpathView.getItems()
                            .addAll(paths.stream().map(f -> f.toString()).filter(p -> !env.getClassPath().contains(p)).collect(Collectors.toList())));
        });

        MenuItem removeSelection = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("removeSelection"));
        removeSelection.disableProperty().bind(classpathView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            classpathView.getItems().removeAll(classpathView.getSelectionModel().getSelectedItems());
        });

        MenuItem copySelection = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("copyPathToClipboard"));
        copySelection.disableProperty().bind(Bindings.size(classpathView.getSelectionModel().getSelectedIndices()).isNotEqualTo(1));
        copySelection.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(classpathView.getSelectionModel().getSelectedItem().replace("\\", "\\\\"));
            clipboard.setContent(content);
        });

        ContextMenu menu = new ContextMenu(add, removeSelection, copySelection);
        classpathView.setContextMenu(menu);
    }

    private void setModulePathContextMenu() {

        MenuItem addDirectory = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("addDirectory"));
        addDirectory.setOnAction(e -> {
            new FileDialog(this)
            .setTitle(FXResourceBundle.getBundle​(JShellApp.class).getString("modulepath"))
                    .directoriesOnly()
                    .showOpenDialog(paths -> modulepathView.getItems()
                            .addAll(paths.stream().map(f -> f.toString()).filter(p -> !env.getModulePath().contains(p))
                                    .collect(Collectors.toList())));
        });

        MenuItem removeSelection = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("removeSelection"));
        removeSelection.disableProperty().bind(modulepathView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            modulepathView.getItems().removeAll(modulepathView.getSelectionModel().getSelectedItems());
        });

        MenuItem copySelection = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("copyPathToClipboard"));
        copySelection.disableProperty().bind(Bindings.size(modulepathView.getSelectionModel().getSelectedIndices()).isNotEqualTo(1));
        copySelection.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(modulepathView.getSelectionModel().getSelectedItem().replace("\\", "\\\\"));
            clipboard.setContent(content);
        });

        ContextMenu menu = new ContextMenu(addDirectory, removeSelection, copySelection);
        modulepathView.setContextMenu(menu);
    }

    private void setExportContextMenu() {

        MenuItem add = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("addExport"));
        add.disableProperty().bind(javafx.beans.binding.Bindings.size(exportModules).lessThan(2));
        add.setOnAction(e -> {
            exportView.getItems().add(
                    new ExportItem(exportModules.get(0), exportModuleReferences.get(exportModules.get(0)).descriptor().packages().iterator().next(),
                            exportModules.get(1)));
        });

        MenuItem removeSelection = new MenuItem(FXResourceBundle.getBundle​(JShellApp.class).getString("removeSelection"));
        removeSelection.disableProperty().bind(exportView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            exportView.getItems().removeAll(exportView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(add, removeSelection);
        exportView.setContextMenu(menu);
    }

    private void setEnv() {
        env.getClassPath().removeIf(p -> Files.notExists(Paths.get(p)));
        classpathView.setItems(FXCollections.observableList(env.getClassPath()));
        env.getModulePath().removeIf(p -> Files.notExists(Paths.get(p)));
        modulepathView.setItems(FXCollections.observableList(env.getModulePath()));

        addModuleView.getTargetItems().setAll(env.getAddModules());

        setModules();

        modulepathView.getItems().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    setModules();
                }
            }
        });
    }

    private Map<String, ModuleReference> getModulePathModuleReferences() {
        List<Path> paths = env.getModulePath().stream().map(p -> Paths.get(p)).collect(Collectors.toList());
        ModuleFinder mf = ModuleFinder.of(paths.toArray(new Path[] {}));

        return mf.findAll().stream().collect(Collectors.toMap(r -> r.descriptor().name(), r -> r));
    }

    private void setModules() {
        modulePathModuleReferences = getModulePathModuleReferences();
        addModuleView.getSourceItems().setAll(modulePathModuleReferences.keySet()
                .stream()
                .filter(m -> !addModuleView.getTargetItems().contains(m))
                .sorted()
                .collect(Collectors.toList()));

        addModuleView.getTargetItems().removeIf(m -> !modulePathModuleReferences.keySet().contains(m));

        exportModuleReferences = new HashMap<>(modulePathModuleReferences.values().stream().filter(r -> !r.descriptor().packages().isEmpty())
                .collect(Collectors.toMap(r -> r.descriptor().name(), r -> r)));
        exportModuleReferences.putAll(systemModuleReferences);
        exportModules.setAll(exportModuleReferences.keySet());

        env.getAddExports().removeIf(e -> !exportModules.contains(e.getSourceModule())
                || e.getTargetModules().removeIf(t -> !exportModules.contains(t)) && e.getTargetModules().isEmpty());
        exportView.setItems(FXCollections.observableList(env.getAddExports()));

        FXCollections.sort(exportModules);
    }

    private void addEnv() {
        env = new Env(FXResourceBundle.getBundle​(JShellApp.class).getString("new"));
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

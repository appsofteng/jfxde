package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import dev.jfxde.jfx.scene.control.AlertBuilder;
import dev.jfxde.jfx.scene.control.TreeViewUtils;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.ui.PathTreeItem;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class FileTreeBox extends VBox {

    private TreeView<FXPath> fileTreeView;
    private PathTreeItem root;
    private ObservableList<TreeItem<FXPath>> selectedItems = FXCollections.observableArrayList();
    private ObservableList<TreeItem<FXPath>> cutItems = FXCollections.observableArrayList();
    private ObservableList<TreeItem<FXPath>> copyItems = FXCollections.observableArrayList();
    private FXPath favoriteRoot;
    private ObjectProperty<Consumer<List<FXPath>>> fileSelectedHandler = new SimpleObjectProperty<>();

    public FileTreeBox(PathTreeItem root, FXPath favorites, Consumer<List<FXPath>> fileSelectedHandler) {
        this.root = root;
        this.favoriteRoot = favorites;
        setFileSelectedHandler(fileSelectedHandler);

        setGraphics();
        setContextMenu();
        setListeners();
    }

    private Consumer<List<FXPath>> getFileSelectedHandler() {
        return fileSelectedHandler.get();
    }

    private void setFileSelectedHandler(Consumer<List<FXPath>> value) {
        fileSelectedHandler.set(value);
    }

    private void setGraphics() {
        fileTreeView = new TreeView<>(root);
        fileTreeView.setShowRoot(false);
        fileTreeView.setMaxHeight(Double.MAX_VALUE);
        fileTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        fileTreeView.setCellFactory(TextFieldTreeCell.forTreeView(new PathDescriptorStringConverter()));

        VBox.setVgrow(fileTreeView, Priority.ALWAYS);

        getChildren().add(fileTreeView);
    }

    private void setListeners() {
        fileTreeView.getSelectionModel().getSelectedItems()
                .addListener((Change<? extends TreeItem<FXPath>> c) -> {
                    while (c.next()) {
                        selectedItems.setAll(TreeViewUtils.getSelectedItemsNoAncestor(fileTreeView));
                    }
                });

        fileTreeView.setOnEditCommit(e -> {
            var oldValue = e.getOldValue();
            FXFiles.rename(oldValue, oldValue.getNewName());
            fileTreeView.getSelectionModel().clearSelection();
            fileTreeView.getSelectionModel().select(e.getTreeItem());

            fileTreeView.setEditable(false);
        });

        cutItems.addListener((Change<? extends TreeItem<FXPath>> c) -> {

            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(i -> i.getGraphic().setDisable(true));
                }
                if (c.wasRemoved()) {
                    c.getRemoved().forEach(i -> i.getGraphic().setDisable(false));
                }
            }
        });

        fileTreeView.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                cutItems.clear();
            }
        });

        fileTreeView.setOnMousePressed(e -> {
            var item = fileTreeView.getSelectionModel().getSelectedItem();

            if (e.getButton() == MouseButton.PRIMARY && item != null) {

                var path = item.getValue();
                if (e.getClickCount() == 2 && path.isFile()) {
                    getFileSelectedHandler().accept(List.of(path));
                }
            }
        });
    }

    private void setContextMenu() {

        MenuItem findFile = new MenuItem();
        FXResourceBundle.getBundle().put(findFile.textProperty(), "findFiles");
        findFile.disableProperty().bind(Bindings.isEmpty(selectedItems));
        findFile.setOnAction(e -> new FindFileDialog(this).show());

        MenuItem newDirectory = new MenuItem();
        FXResourceBundle.getBundle().put(newDirectory.textProperty(), "newDirectory");
        newDirectory.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newDirectory.setOnAction(e -> create(FXFiles::createDirectory, "directory"));

        MenuItem newFile = new MenuItem();
        FXResourceBundle.getBundle().put(newFile.textProperty(), "newFile");
        newFile.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newFile.setOnAction(e -> create(FXFiles::createFile, "file"));

        MenuItem rename = new MenuItem();
        FXResourceBundle.getBundle().put(rename.textProperty(), "rename");
        rename.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        rename.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            fileTreeView.setEditable(true);
            fileTreeView.edit(fileTreeView.getSelectionModel().getSelectedItem());
        });

        MenuItem cut = new MenuItem();
        FXResourceBundle.getBundle().put(cut.textProperty(), "cut");
        cut.disableProperty().bind(Bindings.isEmpty(selectedItems));
        cut.setOnAction(e -> {
            copyItems.clear();
            cutItems.setAll(selectedItems);
        });

        MenuItem copy = new MenuItem();
        FXResourceBundle.getBundle().put(copy.textProperty(), "copy");
        copy.disableProperty().bind(Bindings.isEmpty(selectedItems));
        copy.setOnAction(e -> {
            cutItems.clear();
            copyItems.setAll(selectedItems);
        });

        MenuItem paste = new MenuItem();
        FXResourceBundle.getBundle().put(paste.textProperty(), "paste");
        paste.disableProperty().bind(Bindings.isEmpty(cutItems).and(Bindings.isEmpty(copyItems))
                .or(Bindings.createBooleanBinding(() -> cutItems.stream().anyMatch(i -> i.getParent() == fileTreeView.getSelectionModel().getSelectedItem()), cutItems, fileTreeView.getSelectionModel().selectedItemProperty()))
                .or(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1)));
        paste.setOnAction(e -> {
            var item = fileTreeView.getSelectionModel().getSelectedItem();
            var parentItem = item.getValue().isFile() ? item.getParent() : item;
            var parentPahDescriptor = parentItem.getValue();
            if (cutItems.isEmpty()) {
                var pds = copyItems.stream().map(TreeItem::getValue).collect(Collectors.toList());
                copyItems.clear();
                FXFiles.copy(pds, parentPahDescriptor);
            } else {
                var pds = cutItems.stream().map(TreeItem::getValue).collect(Collectors.toList());
                cutItems.clear();
                FXFiles.move(pds, parentPahDescriptor);
            }
        });

        MenuItem addFavorite = new MenuItem();
        FXResourceBundle.getBundle().put(addFavorite.textProperty(), "addFavorite");
        addFavorite.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1)
                .or(Bindings.createBooleanBinding(() -> fileTreeView.getSelectionModel().getSelectedItem() == null ||
                        fileTreeView.getSelectionModel().getSelectedItem().getValue().isFile() ||
                        favoriteRoot.getPaths().contains(fileTreeView.getSelectionModel().getSelectedItem().getValue()), fileTreeView.getSelectionModel().getSelectedItems(), favoriteRoot.getPaths())));
        addFavorite.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            var item = fileTreeView.getSelectionModel().getSelectedItem();
            favoriteRoot.add(item.getValue());
        });

        MenuItem removeFavorite = new MenuItem();
        FXResourceBundle.getBundle().put(removeFavorite.textProperty(), "removeFavorite");
        removeFavorite.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1)
                .or(Bindings.createBooleanBinding(() -> fileTreeView.getSelectionModel().getSelectedItem() == null ||
                        fileTreeView.getSelectionModel().getSelectedItem().getValue().isFile() ||
                        !favoriteRoot.getPaths().contains(fileTreeView.getSelectionModel().getSelectedItem().getValue()), fileTreeView.getSelectionModel().getSelectedItems(), favoriteRoot.getPaths())));
        removeFavorite.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            var item = fileTreeView.getSelectionModel().getSelectedItem();
            favoriteRoot.remove(item.getValue());
        });

        MenuItem refresh = new MenuItem();
        FXResourceBundle.getBundle().put(refresh.textProperty(), "refresh");
        refresh.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        refresh.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            var item = fileTreeView.getSelectionModel().getSelectedItem();
            item.getValue().refresh();

        });

        MenuItem delete = new MenuItem();
        FXResourceBundle.getBundle().put(delete.textProperty(), "delete");
        delete.disableProperty().bind(Bindings.isEmpty(selectedItems));
        delete.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            var pds = selectedItems.stream().map(TreeItem::getValue).collect(Collectors.toList());
            var notToBeDeleted = pds.stream()
                    .flatMap(p -> p.getNotToBeDeleted().stream())
                    .map(p -> p.getPath().toString())
                    .collect(Collectors.joining("\n"));

            var alert = AlertBuilder.get(this, AlertType.CONFIRMATION)
                    .title(FXResourceBundle.getBundle().getString​("confirmation"))
                    .headerText(FXResourceBundle.getBundle().getString​("areYouSureDeleteSelectedItems"))
                    .ok(() -> FXFiles.delete(pds));

            if (!notToBeDeleted.isEmpty()) {
                alert.contentText(FXResourceBundle.getBundle().getString​("itemsBeingModified"))
                .expandableTextArea(notToBeDeleted);
            }

            alert.show();
        });

        ContextMenu menu = new ContextMenu(findFile, new SeparatorMenuItem(), newDirectory, newFile, rename, new SeparatorMenuItem(), cut, copy, paste, new SeparatorMenuItem(),
                addFavorite, removeFavorite, new SeparatorMenuItem(), refresh,
                new SeparatorMenuItem(), delete);
        fileTreeView.setContextMenu(menu);
    }

    private void create(BiFunction<FXPath, String, FXPath> create, String key) {
        cutItems.clear();
        copyItems.clear();
        var item = fileTreeView.getSelectionModel().getSelectedItem();
        var parentItem = item.getValue().isFile() ? item.getParent() : item;
        var parentPahDescriptor = parentItem.getValue();

        FXPath newPahDescriptor = create.apply(parentPahDescriptor, FXResourceBundle.getBundle().getString​(key));
        TreeViewUtils.select(newPahDescriptor, parentItem, fileTreeView);
    }

    private class PathDescriptorStringConverter extends StringConverter<FXPath> {

        @Override
        public String toString(FXPath pd) {
            return pd == null ? null : pd.toString();
        }

        @Override
        public FXPath fromString(String string) {
            var pd = fileTreeView.getSelectionModel().getSelectedItem().getValue();
            pd.setNewName(string);
            return pd;
        }
    }
}

package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.AlertBuilder;
import dev.jfxde.jfxext.util.TreeViewUtils;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.ui.PathTreeItem;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

public class FileTreeBox extends VBox {

    private TreeView<FXPath> fileTreeView;
    private PathTreeItem root;
    private Map<StringProperty, String> stringProperties = new HashMap<>();
    private Map<String, String> strings = new HashMap<>();
    private ObservableList<TreeItem<FXPath>> selectedItems = FXCollections.observableArrayList();
    private ObservableList<TreeItem<FXPath>> cutItems = FXCollections.observableArrayList();
    private ObservableList<TreeItem<FXPath>> copyItems = FXCollections.observableArrayList();

    public FileTreeBox(PathTreeItem root) {
        this.root = root;

        setGraphics();
        setContextMenu();
        setListeners();
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
    }

    private void setContextMenu() {
        MenuItem newDirectory = new MenuItem("New Directory");
        stringProperties.put(newDirectory.textProperty(), "newDirectory");
        newDirectory.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newDirectory.setOnAction(e -> create(FXFiles::createDirectory));

        MenuItem newFile = new MenuItem("New File");
        stringProperties.put(newFile.textProperty(), "newFile");
        newFile.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newFile.setOnAction(e -> create(FXFiles::createFile));

        MenuItem rename = new MenuItem("Rename");
        stringProperties.put(rename.textProperty(), "rename");
        rename.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        rename.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            fileTreeView.setEditable(true);
            fileTreeView.edit(fileTreeView.getSelectionModel().getSelectedItem());
        });

        MenuItem cut = new MenuItem("Cut");
        stringProperties.put(cut.textProperty(), "cut");
        cut.disableProperty().bind(Bindings.isEmpty(selectedItems));
        cut.setOnAction(e -> {
            copyItems.clear();
            cutItems.setAll(selectedItems);
        });

        MenuItem copy = new MenuItem("Copy");
        stringProperties.put(copy.textProperty(), "copy");
        copy.disableProperty().bind(Bindings.isEmpty(selectedItems));
        copy.setOnAction(e -> {
            cutItems.clear();
            copyItems.setAll(selectedItems);
        });

        MenuItem paste = new MenuItem("Paste");
        stringProperties.put(paste.textProperty(), "paste");
        paste.disableProperty().bind(Bindings.isEmpty(cutItems).and(Bindings.isEmpty(copyItems))
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

        MenuItem delete = new MenuItem("Delete");
        stringProperties.put(delete.textProperty(), "delete");
        delete.disableProperty().bind(Bindings.isEmpty(selectedItems));
        delete.setOnAction(e -> {
            cutItems.clear();
            copyItems.clear();
            var pds = selectedItems.stream().map(TreeItem::getValue).collect(Collectors.toList());

            AlertBuilder.get(this, AlertType.CONFIRMATION)
                    .title(strings.computeIfAbsent("confirmation", k -> "Confirmation"))
                    .headerText(strings.computeIfAbsent("areYouSure", k -> "Are you sure you want to delete the selected items?"))
                    .action(() -> FXFiles.delete(pds))
                    .show();
        });

        ContextMenu menu = new ContextMenu(newDirectory, newFile, rename, new SeparatorMenuItem(), cut, copy, paste,
                new SeparatorMenuItem(), delete);
        fileTreeView.setContextMenu(menu);
    }

    private void create(BiFunction<FXPath, String, FXPath> create) {
        cutItems.clear();
        copyItems.clear();
        var item = fileTreeView.getSelectionModel().getSelectedItem();
        var parentItem = item.getValue().isFile() ? item.getParent() : item;
        var parentPahDescriptor = parentItem.getValue();

        FXPath newPahDescriptor = create.apply(parentPahDescriptor, strings.computeIfAbsent("new", k -> "New"));
        TreeViewUtils.select(newPahDescriptor, parentItem, fileTreeView);
    }

    private class PathDescriptorStringConverter extends StringConverter<FXPath> {

        @Override
        public String toString(FXPath pd) {
            return pd.toString();
        }

        @Override
        public FXPath fromString(String string) {
            var pd = fileTreeView.getSelectionModel().getSelectedItem().getValue();
            pd.setNewName(string);
            return pd;
        }
    }
}

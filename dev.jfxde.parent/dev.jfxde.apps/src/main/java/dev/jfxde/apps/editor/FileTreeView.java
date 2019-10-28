package dev.jfxde.apps.editor;

import java.util.HashMap;
import java.util.Map;

import dev.jfxde.jfxext.control.AlertBuilder;
import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.jfxext.descriptors.PathDescriptor;
import dev.jfxde.jfxext.nio.file.FileUtils;
import dev.jfxde.jfxext.util.TreeViewUtils;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreeView extends VBox {

    private TreeView<PathDescriptor> fileTree;
    private LazyTreeItem<PathDescriptor> root;
    private Map<StringProperty, String> stringProperties = new HashMap<>();
    private Map<String, String> strings = new HashMap<>();
    private ObservableList<TreeItem<PathDescriptor>> selectedItems = FXCollections.observableArrayList();

    public FileTreeView(LazyTreeItem<PathDescriptor> root) {
        this.root = root;

        setGraphics();
        setContextMenu();
        setListeners();
    }

    private void setGraphics() {
        fileTree = new TreeView<>(root);
        fileTree.setShowRoot(false);
        fileTree.setMaxHeight(Double.MAX_VALUE);
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(fileTree, Priority.ALWAYS);

        getChildren().add(fileTree);
    }

    private void setListeners() {
        fileTree.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> selectedItems.setAll(TreeViewUtils.getSelectedItemsNoAncestor(fileTree)));
    }

    private void setContextMenu() {

        MenuItem newDirectory = new MenuItem("New Directory");
        stringProperties.put(newDirectory.textProperty(), "newDirectory");
        newDirectory.disableProperty().bind(Bindings.size(fileTree.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newDirectory.setOnAction(e -> {
            var path = FileUtils.createDirectory(fileTree.getSelectionModel().getSelectedItem().getValue().getPath(),
                    strings.computeIfAbsent("new", k -> "New"));
            ((LazyTreeItem<PathDescriptor>) fileTree.getSelectionModel().getSelectedItem()).insert(new PathDescriptor(path));
        });

        MenuItem delete = new MenuItem("Delete");
        stringProperties.put(delete.textProperty(), "delete");
        delete.disableProperty().bind(Bindings.isEmpty(selectedItems));
        delete.setOnAction(e -> {
            var items = FXCollections.observableArrayList(selectedItems);

            AlertBuilder.get(this, AlertType.CONFIRMATION)
            .title(strings.computeIfAbsent("confirmation", k -> "Confirmation"))
            .headerText(strings.computeIfAbsent("areYouSure", k -> "Are you sure you want to delete the selected items?"))
            .action(() -> {
                items.forEach(i -> {
                    FileUtils.delete(i.getValue().getPath())
                    .thenRun(() -> Platform.runLater(() -> TreeViewUtils.removeFromParent(i)));
                });
            })
            .show();

        });

        ContextMenu menu = new ContextMenu(newDirectory, delete);
        fileTree.setContextMenu(menu);
    }
}

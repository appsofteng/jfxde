package dev.jfxde.apps.editor;

import java.util.HashMap;
import java.util.Map;

import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.jfxext.descriptors.PathDescriptor;
import dev.jfxde.jfxext.nio.file.FileUtils;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreeView extends VBox {

    private TreeView<PathDescriptor> fileTree;
    private LazyTreeItem<PathDescriptor> root;
    private Map<StringProperty, String> stringProperties = new HashMap<>();
    private Map<String, String> strings = new HashMap<>();

    public FileTreeView(LazyTreeItem<PathDescriptor> root) {
        this.root = root;

        setGraphics();
        setContextMenu();
    }

    private void setGraphics() {
        fileTree = new TreeView<>(root);
        fileTree.setShowRoot(false);
        fileTree.setMaxHeight(Double.MAX_VALUE);
        fileTree.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        VBox.setVgrow(fileTree, Priority.ALWAYS);

        getChildren().add(fileTree);
    }

    private void setContextMenu() {
        MenuItem newDirectory = new MenuItem("New Directory");
        stringProperties.put(newDirectory.textProperty(), "newDirectory");
        newDirectory.disableProperty().bind(Bindings.size(fileTree.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newDirectory.setOnAction(e -> {
            var path = FileUtils.createDirectory(fileTree.getSelectionModel().getSelectedItem().getValue().getPath(),
                    strings.computeIfAbsent("new", k -> "New"));
            ((LazyTreeItem<PathDescriptor>)fileTree.getSelectionModel().getSelectedItem()).insert(new PathDescriptor(path));
        });

        ContextMenu menu = new ContextMenu(newDirectory);
        fileTree.setContextMenu(menu);
    }

}

package dev.jfxde.apps.editor;

import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.jfxext.descriptors.PathDescriptor;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreeView extends VBox {

    private TreeView<PathDescriptor> fileTree;
    private LazyTreeItem<PathDescriptor> root;

    public FileTreeView(LazyTreeItem<PathDescriptor> root) {
        this.root = root;

        fileTree = new TreeView<>(root);
        fileTree.setShowRoot(false);
        fileTree.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(fileTree, Priority.ALWAYS);

        getChildren().add(fileTree);
    }
}

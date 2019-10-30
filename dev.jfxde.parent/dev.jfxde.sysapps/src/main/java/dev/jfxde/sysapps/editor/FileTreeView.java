package dev.jfxde.sysapps.editor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import dev.jfxde.jfxext.control.AlertBuilder;
import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.jfxext.nio.file.FileUtils;
import dev.jfxde.jfxext.util.TreeViewUtils;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ChangeListener;
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
    private PathTreeItem root;
    private Map<StringProperty, String> stringProperties = new HashMap<>();
    private Map<String, String> strings = new HashMap<>();
    private ObservableList<TreeItem<PathDescriptor>> selectedItems = FXCollections.observableArrayList();

    public FileTreeView(PathTreeItem root) {
        this.root = root;

        setGraphics();
//        setContextMenu();
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
        fileTree.getSelectionModel().selectedItemProperty()
                .addListener((v, o, n) -> {
                    selectedItems.setAll(TreeViewUtils.getSelectedItemsNoAncestor(fileTree));
                });
    }

//    private void setContextMenu() {
//
//        MenuItem newDirectory = new MenuItem("New Directory");
//        stringProperties.put(newDirectory.textProperty(), "newDirectory");
//        newDirectory.disableProperty().bind(Bindings.size(fileTree.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
//        newDirectory.setOnAction(e -> createPath(FileUtils::createDirectory));
//
//        MenuItem newFile = new MenuItem("New File");
//        stringProperties.put(newDirectory.textProperty(), "newFile");
//        newFile.disableProperty().bind(Bindings.size(fileTree.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
//        newFile.setOnAction(e -> createPath(FileUtils::createFile));
//
//        MenuItem delete = new MenuItem("Delete");
//        stringProperties.put(delete.textProperty(), "delete");
//        delete.disableProperty().bind(Bindings.isEmpty(selectedItems));
//        delete.setOnAction(e -> {
//            var items = FXCollections.observableArrayList(selectedItems);
//
//            AlertBuilder.get(this, AlertType.CONFIRMATION)
//                    .title(strings.computeIfAbsent("confirmation", k -> "Confirmation"))
//                    .headerText(strings.computeIfAbsent("areYouSure", k -> "Are you sure you want to delete the selected items?"))
//                    .action(() -> {
//                        items.forEach(i -> {
//                            FileUtils.delete(i.getValue().getPath())
//                                    .thenRun(() -> Platform.runLater(() -> {
//                                        var parent = i.getParent();
//                                        TreeViewUtils.removeFromParent(i);
//                                        if (parent != null && !parent.isLeaf() && parent.getChildren().isEmpty()) {
//                                            ((LazyTreeItem<PathDescriptor>) parent).setLeaf(true);
//                                            ((LazyTreeItem<PathDescriptor>) parent).setLoaded(false);
//                                            parent.setExpanded(false);
//                                            fileTree.getSelectionModel().clearSelection();
//                                            fileTree.getSelectionModel().select(parent);
//                                        }
//                                    }));
//                        });
//                    }).show();
//
//        });
//
//        ContextMenu menu = new ContextMenu(newDirectory, newFile, delete);
//        fileTree.setContextMenu(menu);
//    }
//
//    private void createPath(BiFunction<Path, String, Path> create) {
//        var item = (LazyTreeItem<PathDescriptor>) fileTree.getSelectionModel().getSelectedItem();
//        var parentItem = item.getValue().isFile() ? (LazyTreeItem<PathDescriptor>) item.getParent() : item;
//        var path = create.apply(parentItem.getValue().getPath(), strings.computeIfAbsent("new", k -> "New"));
//        var newItem = new LazyTreeItem<>(new PathDescriptor(path), parentItem);
//
//        if (parentItem.isLoaded()) {
//            TreeViewUtils.insert(newItem, parentItem);
//            parentItem.setExpanded(true);
//            fileTree.getSelectionModel().clearSelection();
//            fileTree.getSelectionModel().select(newItem);
//        } else {
//            if (parentItem.isLeaf()) {
//                parentItem.setLeaf(null);
//            }
//
//            ChangeListener<Boolean>[] loadedListener = new ChangeListener[1];
//
//            loadedListener[0] = (v, o, n) -> {
//                if (n) {
//                    fileTree.getSelectionModel().clearSelection();
//                    var loadedNewItem = parentItem.getChildren().stream().filter(c -> c.equals(newItem)).findFirst().get();
//                    fileTree.getSelectionModel().select(loadedNewItem);
//                    v.removeListener(loadedListener[0]);
//                }
//            };
//            parentItem.loadedProperty().addListener(loadedListener[0]);
//            parentItem.setExpanded(true);
//        }
//    }
}

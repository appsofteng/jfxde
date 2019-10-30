package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import dev.jfxde.jfxext.control.AlertBuilder;
import dev.jfxde.jfxext.util.TreeViewUtils;
import dev.jfxde.logic.data.PathDescriptor;
import dev.jfxde.logic.data.PathDescriptors;
import dev.jfxde.ui.PathTreeItem;
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

public class FileTreeBox extends VBox {

    private TreeView<PathDescriptor> fileTreeView;
    private PathTreeItem root;
    private Map<StringProperty, String> stringProperties = new HashMap<>();
    private Map<String, String> strings = new HashMap<>();
    private ObservableList<TreeItem<PathDescriptor>> selectedItems = FXCollections.observableArrayList();

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
        VBox.setVgrow(fileTreeView, Priority.ALWAYS);

        getChildren().add(fileTreeView);
    }

    private void setListeners() {
        fileTreeView.getSelectionModel().selectedItemProperty()
                .addListener((v, o, n) -> {
                    selectedItems.setAll(TreeViewUtils.getSelectedItemsNoAncestor(fileTreeView));
                });
    }

    private void setContextMenu() {
        MenuItem newDirectory = new MenuItem("New Directory");
        stringProperties.put(newDirectory.textProperty(), "newDirectory");
        newDirectory.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newDirectory.setOnAction(e -> create(PathDescriptors::createDirectory));

        MenuItem newFile = new MenuItem("New File");
        stringProperties.put(newDirectory.textProperty(), "newFile");
        newFile.disableProperty().bind(Bindings.size(fileTreeView.getSelectionModel().getSelectedItems()).isNotEqualTo(1));
        newFile.setOnAction(e -> create(PathDescriptors::createFile));

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
                            var parent = i.getParent();
                            var sibling = i.previousSibling() != null ? i.previousSibling() : i.nextSibling();
                            PathDescriptors.delete(i.getValue())
                            .thenRun(() -> Platform.runLater(() -> {
                                if (parent.getChildren().isEmpty()) {
                                    parent.setExpanded(false);
                                    fileTreeView.getSelectionModel().clearSelection();
                                    fileTreeView.getSelectionModel().select(parent);
                                } else {
                                    fileTreeView.getSelectionModel().clearSelection();
                                    fileTreeView.getSelectionModel().select(sibling);
                                }
                            }));
                        });
                    }).show();
        });

        ContextMenu menu = new ContextMenu(newDirectory, newFile, delete);
        fileTreeView.setContextMenu(menu);
    }

    private void create(BiFunction<PathDescriptor, String, PathDescriptor> create) {
        var item = fileTreeView.getSelectionModel().getSelectedItem();
        var parentItem = item.getValue().isFile() ? item.getParent() : item;
        var parentPahDescriptor = parentItem.getValue();

        PathDescriptor newPahDescriptor = create.apply(parentPahDescriptor, strings.computeIfAbsent("new", k -> "New"));
        TreeViewUtils.select(newPahDescriptor, parentItem, fileTreeView);
    }
}

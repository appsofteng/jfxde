package dev.jfxde.jfx.scene.control;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public final class TreeViewUtils {

    private TreeViewUtils() {
    }

    public static <T> ObservableList<TreeItem<T>> getSelectedItemsNoAncestor(TreeView<T> treeView, Predicate<TreeItem<T>> filter) {
        ObservableList<TreeItem<T>> items = FXCollections.emptyObservableList();
        var selectedItems = treeView.getSelectionModel().getSelectedItems();

        if (!selectedItems.isEmpty()) {

            List<TreeItem<T>> selectedItemsNoAncestor = selectedItems
                    .stream()
                    .filter(filter)
                    .takeWhile(i -> !containsAncestor(i, selectedItems))
                    .collect(Collectors.toList());

            if (selectedItemsNoAncestor.size() == selectedItems.size()) {
                items = FXCollections.observableArrayList(selectedItemsNoAncestor);
            }
        }

        return items;
    }

    public static <T> void select(TreeView<T> treeView, TreeItem<T> parent, T value) {
        var item = parent.getChildren().stream().filter(i -> i.getValue().equals(value)).findFirst().orElse(null);

        if (item != null) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(item);
        }
    }

    public static void removeFromParent(TreeItem<?> item) {
        if (item.getParent() != null) {
            item.getParent().getChildren().remove(item);
        }
    }

    private static <T> boolean containsAncestor(TreeItem<T> item, ObservableList<TreeItem<T>> items) {
        TreeItem<T> ancestor = item.getParent();
        var result = false;

        while (ancestor != null) {
            if (items.contains(ancestor)) {
                result = true;
                break;
            }

            ancestor = ancestor.getParent();
        }

        return result;
    }
}

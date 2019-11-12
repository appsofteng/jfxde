package dev.jfxde.jfx.scene.control;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

public final class TreeViewUtils {

    private TreeViewUtils() {
    }

    public static <T> ObservableList<TreeItem<T>> getSelectedItemsNoAncestor(TreeView<T> treeView) {
        ObservableList<TreeItem<T>> items = FXCollections.emptyObservableList();
        var selectedItems = treeView.getSelectionModel().getSelectedItems();

        if (!selectedItems.isEmpty()) {

            List<TreeItem<T>> selectedItemsNoAncestor = selectedItems
                    .stream()
                    .takeWhile(i -> !containsAncestor(i, selectedItems))
                    .collect(Collectors.toList());

            if (selectedItemsNoAncestor.size() == selectedItems.size()) {
                items = FXCollections.observableArrayList(selectedItemsNoAncestor);
            }
        }

        return items;
    }

    public static <T> void select(T value, TreeItem<T> parent, TreeView<T> treeView) {
        var item = parent.getChildren().stream().filter(i -> i.getValue().equals(value)).findFirst().orElse(null);

        if (item != null) {
            treeView.getSelectionModel().clearSelection();
            treeView.getSelectionModel().select(item);
        }
    }

    public static <T extends Comparable<T>> void insert(TreeItem<T> item, TreeItem<T> parent) {
        var index = IntStream.range(0, parent.getChildren().size())
                .filter(i -> parent.getChildren().get(i).getValue().compareTo(item.getValue()) > 0)
                .findFirst()
                .orElse(-1);
        if (index > -1) {
            parent.getChildren().add(index, item);
        } else {
            parent.getChildren().add(item);
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

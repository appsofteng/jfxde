package dev.jfxde.jfxext.util;

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

        if (!treeView.getSelectionModel().getSelectedItems().isEmpty()) {

            List<TreeItem<T>> selectedItems = treeView.getSelectionModel().getSelectedItems()
                    .stream()
                    .takeWhile(i -> !containsAncestor(i, treeView.getSelectionModel().getSelectedItems()))
                    .collect(Collectors.toList());

            if (selectedItems.size() == treeView.getSelectionModel().getSelectedItems().size()) {
                items = FXCollections.observableArrayList(selectedItems);
            }
        }

        return items;
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

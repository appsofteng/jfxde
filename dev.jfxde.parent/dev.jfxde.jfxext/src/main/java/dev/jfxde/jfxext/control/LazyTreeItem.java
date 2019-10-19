package dev.jfxde.jfxext.control;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class LazyTreeItem<T> extends TreeItem<T> {

    private Function<LazyTreeItem<T>, Boolean> leaf;
    private Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> childrenGetter = i -> List.of();
    private Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> filteredChildrenGetter = i -> List.of();
    private Function<LazyTreeItem<T>, Node> graphic = i -> null;
    private Function<LazyTreeItem<T>, String> toString = i -> i.toString();
    private ForkJoinTask<Void> loaded;
    private boolean allLoaded;
    private Boolean isLeaf;
    private ObservableList<LazyTreeItem<T>> allChildren = FXCollections.observableArrayList();

    public LazyTreeItem(T value) {
        super(value);
    }

    public LazyTreeItem(T value, LazyTreeItem<T> parent) {
        super(value);
        this.leaf = parent.leaf;
        this.childrenGetter = parent.childrenGetter;
        this.filteredChildrenGetter = parent.filteredChildrenGetter;
        this.toString = parent.toString;
        graphic(parent.graphic);
    }

    public LazyTreeItem<T> leaf(Function<LazyTreeItem<T>, Boolean> leaf) {
        this.leaf = leaf;

        return this;
    }

    public LazyTreeItem<T> childrenGetter(Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> childrenGetter) {
        this.childrenGetter = childrenGetter;

        return this;
    }

    public LazyTreeItem<T> filteredChildrenGetter(Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> filteredChildrenGetter) {
        this.filteredChildrenGetter = filteredChildrenGetter;

        return this;
    }

    public LazyTreeItem<T> toString(Function<LazyTreeItem<T>, String> toString) {
        this.toString = toString;

        return this;
    }

    public LazyTreeItem<T> graphic(Function<LazyTreeItem<T>, Node> graphic) {
        this.graphic = graphic;
        setGraphic(graphic.apply(this));

        return this;
    }

    @Override
    public boolean isLeaf() {

        if (isLeaf == null) {
            isLeaf = leaf == null ? super.isLeaf() : leaf.apply(this);
        }

        return isLeaf;
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {

        if (loaded == null) {
            load();
        }

        return super.getChildren();
    }

    @Override
    public String toString() {
        return toString.apply(this);
    }

    private void load() {
        var task = Executors.<Void>privilegedCallable(() -> {

            List<LazyTreeItem<T>> children = childrenGetter.apply(this);
            Platform.runLater(() -> {
                super.getChildren().setAll(children);
                allChildren.setAll(children);
            });

            return null;
        });

        loaded = ForkJoinPool.commonPool().submit(task);
    }

    public ObservableList<LazyTreeItem<T>> getAllChildren() {
        if (!allLoaded) {
            allLoaded = true;

            if (loaded == null) {
                load();
            }

            var task = Executors.privilegedCallable(() -> {

                loaded.join();
                List<LazyTreeItem<T>> filteredChidren = filteredChildrenGetter.apply(this);

                Platform.runLater(() -> {

                    allChildren.addAll(filteredChidren);
                });

                return null;
            });

            ForkJoinPool.commonPool().submit(task);
        }

        return allChildren;
    }
}

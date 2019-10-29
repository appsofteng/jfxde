package dev.jfxde.jfxext.control;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.function.Consumer;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class LazyTreeItem<T> extends TreeItem<T> {

    private Function<LazyTreeItem<T>, Boolean> leaf;
    private Consumer<LazyTreeItem<T>> childrenGetter = i -> {
    };
    private Consumer<LazyTreeItem<T>> filteredChildrenGetter = i -> {
    };
    private Function<LazyTreeItem<T>, Node> graphic = i -> null;
    private Function<LazyTreeItem<T>, String> toString = i -> i.toString();
    private ForkJoinTask<Void> loadedTask;
    private boolean loading;
    private BooleanProperty loaded = new SimpleBooleanProperty();
    private boolean allLoading;
    private Boolean isLeaf;
    private ObservableList<LazyTreeItem<T>> allChildren = FXCollections.observableArrayList();
    private List<LazyTreeItem<T>> cache = new ArrayList<>();
    private List<LazyTreeItem<T>> filteredCache = new ArrayList<>();
    private static final int CACHE_SIZE = 40;

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

    public void setChildren(List<TreeItem<T>> value) {
        super.getChildren().setAll(value);
        setLoaded(true);
    }

    public LazyTreeItem<T> leaf(Function<LazyTreeItem<T>, Boolean> leaf) {
        this.leaf = leaf;

        return this;
    }

    public LazyTreeItem<T> childrenGetter(Consumer<LazyTreeItem<T>> childrenGetter) {
        this.childrenGetter = childrenGetter;

        return this;
    }

    public LazyTreeItem<T> filteredChildrenGetter(Consumer<LazyTreeItem<T>> filteredChildrenGetter) {
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

    public void setLeaf(Boolean value) {
        isLeaf = value;
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {

        if (!loading && !isLoaded()) {
            load();
        }

        return super.getChildren();
    }

    public boolean isLoaded() {
        return loaded.get();
    }

    public void setLoaded(boolean value) {
        loaded.set(value);
    }

    public ReadOnlyBooleanProperty loadedProperty() {
        return loaded;
    }

    @Override
    public String toString() {
        return toString.apply(this);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TreeItem)) {
            return false;
        }
        return getValue().equals(((TreeItem<T>)obj).getValue());
    }

    @Override
    public int hashCode() {
        return getValue().hashCode();
    }

    private void load() {
        loading = true;
        cache.clear();
        var task = Executors.<Void>privilegedCallable(() -> {

            childrenGetter.accept(this);
            return null;
        });

        loadedTask = ForkJoinPool.commonPool().submit(task);
    }

    public void addCached(LazyTreeItem<T> child) {

        if (cache.size() < CACHE_SIZE && child != null) {
            cache.add(child);
        } else {
            if (!cache.isEmpty()) {
                var copy = new ArrayList<>(cache);
                cache.clear();
                Platform.runLater(() -> {
                    super.getChildren().addAll(copy);
                    allChildren.addAll(copy);
                });
            }

            if (child == null) {
                Platform.runLater(() -> {
                    loading = false;
                    setLoaded(true);
                });
            }
        }
    }

    public void addFilteredCached(LazyTreeItem<T> child) {

        if (filteredCache.size() < CACHE_SIZE && child != null) {
            filteredCache.add(child);
        } else {
            if (!filteredCache.isEmpty()) {
                var copy = new ArrayList<>(filteredCache);
                filteredCache.clear();
                Platform.runLater(() -> {
                    allChildren.addAll(copy);
                });
            }
        }
    }

    public ObservableList<LazyTreeItem<T>> getAllChildren() {
        if (!allLoading) {
            allLoading = true;
            filteredCache.clear();

            if (!loading && !isLoaded()) {
                load();
            }

            var task = Executors.privilegedCallable(() -> {

                loadedTask.join();
                filteredChildrenGetter.accept(this);

                return null;
            });

            ForkJoinPool.commonPool().submit(task);
        }

        return allChildren;
    }
}

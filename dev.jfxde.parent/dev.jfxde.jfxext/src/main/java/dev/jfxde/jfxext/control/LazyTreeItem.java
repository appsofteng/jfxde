package dev.jfxde.jfxext.control;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class LazyTreeItem<T> extends TreeItem<T> {

    private Function<LazyTreeItem<T>, Boolean> leaf;
    private Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> childrenGetter = i -> List.of();
    private Function<LazyTreeItem<T>, Node> graphic = i -> null;
    private AtomicBoolean loaded = new AtomicBoolean();
    private Boolean isLeaf;

    public LazyTreeItem(T value) {
        super(value);
    }

    public LazyTreeItem(T value, LazyTreeItem<T> parent) {
        super(value);
        this.leaf = parent.leaf;
        this.childrenGetter = parent.childrenGetter;
        graphic(parent.graphic);

//        Platform.runLater(() -> parent.getChildren().add(this));
    }

    public LazyTreeItem<T> leaf(Function<LazyTreeItem<T>, Boolean> leaf) {
        this.leaf = leaf;

        return this;
    }

    public LazyTreeItem<T> childrenGetter(Function<LazyTreeItem<T>, List<LazyTreeItem<T>>> childrenGetter) {
        this.childrenGetter = childrenGetter;

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

        if (!loaded.getAndSet(true)) {

            var task = Executors.privilegedCallable(() -> {

                List<LazyTreeItem<T>> items = childrenGetter.apply(this);
                Platform.runLater(() -> super.getChildren().setAll(items));

                return null;
            });

            ForkJoinPool.commonPool().submit(task);
        }

        return super.getChildren();
    }
}

package dev.jfxde.jfxext.control;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class LazyTreeItem<T> extends TreeItem<T> {

    private Function<T,Boolean> leaf;
    private Function<T, List<T>> childrenGetter;
    private boolean loaded;

    public LazyTreeItem(T value, Function<T,Boolean> leaf, Function<T, List<T>> childrenGetter) {
        super(value);

        this.leaf = leaf;
        this.childrenGetter = childrenGetter;
    }

    @Override
    public boolean isLeaf() {
        return leaf.apply(getValue());
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {

        if (!loaded) {
            loaded = true;
            List<T> children = childrenGetter.apply(getValue());
            List<LazyTreeItem<T>> items = children.stream().map(c -> new LazyTreeItem<>(c, leaf, childrenGetter)).collect(Collectors.toList());

            super.getChildren().setAll(items);
        }

        return super.getChildren();
    }
}

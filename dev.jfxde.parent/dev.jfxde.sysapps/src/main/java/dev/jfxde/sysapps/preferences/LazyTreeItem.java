package dev.jfxde.sysapps.preferences;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import dev.jfxde.logic.data.Treeable;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;

public class LazyTreeItem<T extends Treeable> extends TreeItem<T> {

    private Function<T, List<T>> childrenGetter;
    private boolean loaded;

    public LazyTreeItem(T value, Function<T, List<T>> childrenGetter) {
        super(value);

        this.childrenGetter = childrenGetter;

    }

    @Override
    public boolean isLeaf() {
        return getValue().isLeaf();
    }

    @Override
    public ObservableList<TreeItem<T>> getChildren() {

        if (!loaded) {
            loaded = true;
            List<T> children = childrenGetter.apply(getValue());
            List<LazyTreeItem<T>> items = children.stream().map(c -> new LazyTreeItem<>(c, childrenGetter)).collect(Collectors.toList());

            super.getChildren().setAll(items);
        }

        return super.getChildren();
    }

}

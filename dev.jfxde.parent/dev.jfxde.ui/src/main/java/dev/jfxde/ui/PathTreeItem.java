package dev.jfxde.ui;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.logic.data.FXPath;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends TreeItem<FXPath> {

    private boolean dirOnly;
    private ObservableList<TreeItem<FXPath>> allChildren = FXCollections.observableArrayList((i) -> new Observable[] { i.getValue().nameProperty() });
    private ObservableList<TreeItem<FXPath>> sortedChildren;
    private boolean loading;
    private BooleanProperty loaded = new SimpleBooleanProperty();

    private ListChangeListener<FXPath> pathListener = (Change<? extends FXPath> c) -> {

        while (c.next()) {

            if (c.wasAdded()) {
                addItems(c.getAddedSubList());
            } else if (c.wasRemoved()) {
                removeItems(c.getRemoved());
            }
        }
    };

    private ChangeListener<Boolean> pathLoadedListener = (v, o, n) -> {
        if (n) {
            XPlatform.runFX(() -> {
                if (isExpanded()) {
                    allChildren.clear();
                    addItems(getValue().getPaths());
                    setLoaded(true);
                    getValue().getPaths().addListener(pathListener);
                } 

                loading = false;
            });
        }
    };
    
    private ChangeListener<Boolean> leafListener = (v, o, n) -> {
        if (!n) {
            XPlatform.runFX(() -> {
                   allChildren.add(new PathTreeItem());
            });
        }
    };    

    private PathTreeItem() {
        super(FXPath.getPseudoPath(List.of()));
    }

    public PathTreeItem(FXPath path) {
        this(path, false);
    }

    public PathTreeItem(FXPath path, boolean dirOnly) {
        super(path);
        this.dirOnly = dirOnly;
        setGraphic(path.getGraphic());

        var filteredChildren = dirOnly ? new FilteredList<>(allChildren, i -> i.getValue().isDirectory()) : allChildren;
        sortedChildren = new SortedList<>(filteredChildren, Comparator.comparing(i -> i.getValue()));
        Bindings.bindContent(super.getChildren(), sortedChildren);

        path.loadedProperty().addListener(pathLoadedListener);
        if (dirOnly) {
            path.dirLeafProperty().addListener(leafListener);
        } else {
            path.leafProperty().addListener(leafListener);
        }

        if (path.isLoaded()) {
            loading = true;
            Platform.runLater(() -> {
                addItems(getValue().getPaths());
                setLoaded(true);
                loading = false;
                getValue().getPaths().addListener(pathListener);
            });
        }
    }

    private void addItems(List<? extends FXPath> paths) {
        XPlatform.runFX(() -> paths.stream()
                .map(p -> new PathTreeItem(p, dirOnly))
                .forEach(i -> allChildren.add(i)));
    }

    private void removeItems(List<? extends FXPath> paths) {
        XPlatform.runFX(() -> {
            allChildren.removeIf(i -> {
                return ((PathTreeItem) i).remove(paths);
            });

            if (super.getChildren().isEmpty() && getParent() != null) {
                setExpanded(false);
                setLoaded(false);
                getValue().getPaths().removeListener(pathListener);
            }
        });
    }

    private boolean remove(List<? extends FXPath> paths) {
        boolean remove = paths.contains(getValue());
        if (remove) {
            getValue().getPaths().removeListener(pathListener);
            getValue().loadedProperty().addListener(pathLoadedListener);
            if (dirOnly) {
                getValue().dirLeafProperty().removeListener(leafListener);
            } else {
                getValue().leafProperty().removeListener(leafListener);
            }
        }
        return remove;
    }

    private boolean isLoaded() {
        return loaded.get();
    }

    private void setLoaded(boolean value) {
        this.loaded.set(value);
    }

    @Override
    public ObservableList<TreeItem<FXPath>> getChildren() {
        checkLoaded();
        return super.getChildren();
    }

    public ObservableList<TreeItem<FXPath>> getAllChildren() {
        checkLoaded();
        return allChildren;
    }

    private void checkLoaded() {

        if (!loading && !isLoaded()) {
            loading = true;
            if (!getValue().isLoaded()) {
                getValue().load();
            } else {
                allChildren.clear();
                addItems(getValue().getPaths());
                setLoaded(true);
                loading = false;
                getValue().getPaths().addListener(pathListener);
            }
        }
    }

    private ChangeListener<Boolean> loadedListener;

    public void traverse(Predicate<TreeItem<FXPath>> predicate) {

        if (isLoaded()) {
            traverseLoaded(predicate);
        } else {

            loadedListener = (v, o, n) -> {
                if (n) {
                    loaded.removeListener(loadedListener);
                    traverseLoaded(predicate);
                }
            };

            loaded.addListener(loadedListener);
            getChildren();
        }
    }

    private void traverseLoaded(Predicate<TreeItem<FXPath>> predicate) {
        PathTreeItem result = (PathTreeItem) getChildren().stream().filter(predicate).findFirst().orElse(null);

        if (result != null) {
            result.setExpanded(true);
            result.traverse(predicate);
        }
    }

    @Override
    public boolean isLeaf() {
        return dirOnly ? getValue().isDirLeaf() : getValue().isLeaf();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}

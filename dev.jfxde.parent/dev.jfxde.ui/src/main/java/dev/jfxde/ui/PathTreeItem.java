package dev.jfxde.ui;

import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

import dev.jfxde.jfxext.util.FXUtils;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends TreeItem<PathDescriptor> {

    private Function<PathDescriptor, Node> graphicFactory;
    private boolean dirOnly;
    private ObservableList<TreeItem<PathDescriptor>> allChildren = FXCollections.observableArrayList();
    private ObservableList<TreeItem<PathDescriptor>> sortedAllChildren = new SortedList<>(allChildren, Comparator.comparing(i -> i.getValue()));

    public PathTreeItem(PathDescriptor descriptor) {
        this(descriptor, p -> FXUtils.getIcon(p.getPath()), false);
    }

    public PathTreeItem(PathDescriptor descriptor, boolean dirOnly) {
        this(descriptor, p -> FXUtils.getIcon(p.getPath()), dirOnly);
    }

    public PathTreeItem(PathDescriptor descriptor, Function<PathDescriptor, Node> graphicFactory, boolean dirOnly) {
        super(descriptor);
        this.graphicFactory = graphicFactory;
        this.dirOnly = dirOnly;
        setGraphic(graphicFactory.apply(descriptor));

        Platform.runLater(() -> {
            addItems(getValue().getPaths());
            setListeners();
        });
    }

    private void setListeners() {
        getValue().nameProperty().addListener((v,o,n) -> {
            var parent = (PathTreeItem)getParent();

            if (parent == null) {
                return;
            }

            parent.getChildren().remove(this);

            var index =  parent.sortedAllChildren.indexOf(this);
            parent.getChildren().add(index, this);
        });

        getValue().getPaths().addListener((Change<? extends PathDescriptor> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    FXUtils.runFX(() -> addItems(c.getAddedSubList()));
                } else if (c.wasRemoved()) {
                    FXUtils.runFX(() -> removeItems(c.getRemoved()));
                }
            }
        });
    }

    private void addItems(List<? extends PathDescriptor> pds) {
        pds.forEach(pd -> {
            var item = new PathTreeItem(pd, graphicFactory, dirOnly);
            allChildren.add(item);
            var index =  sortedAllChildren.indexOf(item);

            if (dirOnly) {
                if (pd.isDirectory()) {
                    super.getChildren().add(index, item);
                }
            } else {
                super.getChildren().add(index, item);
            }
        });
    }

    private void removeItems(List<? extends PathDescriptor> pds) {
        super.getChildren().removeIf(i -> pds.contains(i.getValue()));
        allChildren.removeIf(i -> pds.contains(i.getValue()));
        if (super.getChildren().isEmpty()) {
            setExpanded(false);
        }
    }

    @Override
    public ObservableList<TreeItem<PathDescriptor>> getChildren() {
        getValue().load();
        return super.getChildren();
    }

    public ObservableList<TreeItem<PathDescriptor>> getAllChildren() {
        getValue().load();
        return allChildren;
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

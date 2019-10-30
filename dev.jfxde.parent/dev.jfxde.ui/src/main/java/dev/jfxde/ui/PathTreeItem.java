package dev.jfxde.ui;

import java.util.List;
import java.util.function.Function;
import java.util.stream.IntStream;

import dev.jfxde.jfxext.util.FXUtils;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends TreeItem<PathDescriptor> {

    private Function<PathDescriptor, Node> graphicFactory;
    private boolean dirOnly;
    private ObservableList<PathTreeItem> children = FXCollections.observableArrayList();
    private ObservableList<PathTreeItem> allChildren = FXCollections.observableArrayList();

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

        addItems(getValue().getPaths());

        setListeners();
    }

    private void setListeners() {
        getValue().getPaths().addListener((Change<? extends PathDescriptor> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    if (Platform.isFxApplicationThread()) {
                        addItems(c.getAddedSubList());
                    } else {
                        Platform.runLater(() -> addItems(c.getAddedSubList()));
                    }
                } else if (c.wasRemoved()) {
                    if (Platform.isFxApplicationThread()) {
                        removeItems(c.getRemoved());
                    } else {
                        Platform.runLater(() -> removeItems(c.getRemoved()));
                    }
                }
            }
        });
    }

    private void addItems(List<? extends PathDescriptor> pds) {
        pds.forEach(pd -> {
            var item = new PathTreeItem(pd, graphicFactory, dirOnly);
            var index = IntStream.range(0, allChildren.size())
                    .filter(i -> allChildren.get(i).getValue().compareTo(item.getValue()) > 0)
                    .findFirst()
                    .orElse(-1);
            if (index > -1) {
                allChildren.add(index, item);
            } else {
                allChildren.add(item);
            }

            if (dirOnly) {
                if (pd.isDirectory()) {
                    if (index > -1) {
                        children.add(index, item);
                        super.getChildren().add(index, item);
                    } else {
                        children.add(item);
                        super.getChildren().add(item);
                    }
                }
            } else {
                if (index > -1) {
                    children.add(index, item);
                    super.getChildren().add(index, item);
                } else {
                    children.add(item);
                    super.getChildren().add(item);
                }
            }
        });
    }

    private void removeItems(List<? extends PathDescriptor> pds) {
        super.getChildren().removeIf(i -> pds.contains(i.getValue()));
        children.removeIf(i -> pds.contains(i.getValue()));
        allChildren.removeIf(i -> pds.contains(i.getValue()));
    }

    @Override
    public ObservableList<TreeItem<PathDescriptor>> getChildren() {
        getValue().load();
        return super.getChildren();
    }

    public ObservableList<PathTreeItem> getItems() {
        getValue().load();
        return children;
    }

    public ObservableList<PathTreeItem> getAllChildren() {
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

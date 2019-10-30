package dev.jfxde.sysapps.editor;

import java.util.function.Function;

import dev.jfxde.logic.data.PathDescriptor;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class PathTreeItem extends TreeItem<PathDescriptor> {

    public PathTreeItem(PathDescriptor descriptor, Function<PathDescriptor, Node> graphicFactory) {
        super(descriptor);
        setGraphic(graphicFactory.apply(descriptor));
        descriptor.getPaths().forEach(pd -> getChildren().add(new PathTreeItem(pd, graphicFactory)));

        descriptor.getPaths().addListener((Change<? extends PathDescriptor> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    Platform.runLater(() -> c.getAddedSubList().forEach(pd -> getChildren().add(new PathTreeItem(pd, graphicFactory))));
                }
            }
        });

        descriptor.load();
    }

    @Override
    public boolean isLeaf() {
        return getValue().isLeaf();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}

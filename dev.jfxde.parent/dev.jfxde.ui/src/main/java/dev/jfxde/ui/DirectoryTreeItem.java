package dev.jfxde.ui;

import java.util.function.Function;

import dev.jfxde.logic.data.PathDescriptor;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.Node;
import javafx.scene.control.TreeItem;

public class DirectoryTreeItem extends TreeItem<PathDescriptor> {

    public DirectoryTreeItem(PathDescriptor descriptor, Function<PathDescriptor, Node> graphicFactory) {
        super(descriptor);
        setGraphic(graphicFactory.apply(descriptor));
        descriptor.getDirectories().forEach(pd -> getChildren().add(new DirectoryTreeItem(pd, graphicFactory)));

        descriptor.getDirectories().addListener((Change<? extends PathDescriptor> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    Platform.runLater(() -> c.getAddedSubList().forEach(pd -> getChildren().add(new DirectoryTreeItem(pd, graphicFactory))));
                }
            }
        });

        descriptor.load();
    }

    @Override
    public boolean isLeaf() {
        return getValue().isDirLeaf();
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}

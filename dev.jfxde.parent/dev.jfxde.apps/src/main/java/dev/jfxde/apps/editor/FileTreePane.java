package dev.jfxde.apps.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.jfxext.descriptors.PathDescriptor;
import dev.jfxde.jfxext.util.FXUtils;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreePane extends VBox {

    private Accordion accordion = new Accordion();
    private Map<StringProperty, String> strings = new HashMap<>();

    public FileTreePane(List<String> bookmarkPaths) {
        accordion.getPanes().addAll(createFilesPane(), createBookmarksPane(bookmarkPaths));
        accordion.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(accordion, Priority.ALWAYS);
        getChildren().add(accordion);
    }

    private TitledPane createFilesPane() {

        LazyTreeItem<PathDescriptor> root = new LazyTreeItem<>(new PathDescriptor())
                .leaf(i -> i.getValue().isLeaf())
                .childrenGetter(i -> i.getValue().getAll(p -> new LazyTreeItem<>(p, i), c -> i.addCached(c)))
                .toString(i -> i.getValue().getPath().toString())
                .graphic(i -> FXUtils.getIcon(i.getValue().getPath()));

        FileTreeView fileTree = new FileTreeView(root);

        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Files");
        strings.put(pane.textProperty(), "files");
        pane.setContent(fileTree);

        return pane;
    }

    private TitledPane createBookmarksPane(List<String> bookmarkPaths) {

        LazyTreeItem<PathDescriptor> root = new LazyTreeItem<>(new PathDescriptor())
                .leaf(i -> i.getValue().isLeaf())
                .childrenGetter(i -> i.getValue().getAll(p -> new LazyTreeItem<>(p, i), c -> i.addCached(c)))
                .toString(i -> i.getValue().getPath().toString())
                .graphic(i -> FXUtils.getIcon(i.getValue().getPath()));

        List<TreeItem<PathDescriptor>> children = bookmarkPaths.stream().map(p -> new LazyTreeItem<>(new PathDescriptor(p), root)).collect(Collectors.toList());
        root.setChildren(children);

        FileTreeView fileTree = new FileTreeView(root);
        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Bookmarks");
        strings.put(pane.textProperty(), "bookmarks");
        pane.setContent(fileTree);

        return pane;
    }
}

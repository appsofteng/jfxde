package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.util.FXUtils;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
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

        PathTreeItem root = new PathTreeItem(PathDescriptor.getRoot(), pd -> FXUtils.getIcon(pd.getPath()));

        FileTreeView fileTree = new FileTreeView(root);

        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Files");
        strings.put(pane.textProperty(), "files");
        pane.setContent(fileTree);

        return pane;
    }

    private TitledPane createBookmarksPane(List<String> bookmarkPaths) {

        PathTreeItem root = new PathTreeItem(PathDescriptor.getEmpty(), pd -> FXUtils.getIcon(pd.getPath()));

        List<PathTreeItem> children = bookmarkPaths.stream().map(p -> new PathTreeItem(PathDescriptor.get(p), pd -> FXUtils.getIcon(pd.getPath())))
                .collect(Collectors.toList());
        root.getChildren().setAll(children);

        FileTreeView fileTree = new FileTreeView(root);
        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Bookmarks");
        strings.put(pane.textProperty(), "bookmarks");
        pane.setContent(fileTree);

        return pane;
    }
}

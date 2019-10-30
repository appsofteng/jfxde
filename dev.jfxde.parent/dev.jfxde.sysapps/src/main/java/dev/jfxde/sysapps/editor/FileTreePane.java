package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.jfxde.logic.data.PathDescriptor;
import dev.jfxde.ui.PathTreeItem;
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

        PathTreeItem root = new PathTreeItem(PathDescriptor.getRoot());

        FileTreeBox fileTree = new FileTreeBox(root);

        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Files");
        strings.put(pane.textProperty(), "files");
        pane.setContent(fileTree);

        return pane;
    }

    private TitledPane createBookmarksPane(List<String> bookmarkPaths) {

        PathTreeItem root = new PathTreeItem(PathDescriptor.getNoname(bookmarkPaths));

        FileTreeBox fileTree = new FileTreeBox(root);
        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Bookmarks");
        strings.put(pane.textProperty(), "bookmarks");
        pane.setContent(fileTree);

        return pane;
    }
}

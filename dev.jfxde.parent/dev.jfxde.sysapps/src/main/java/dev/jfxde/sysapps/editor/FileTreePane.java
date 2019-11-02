package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import dev.jfxde.logic.data.FXPath;
import dev.jfxde.ui.PathTreeItem;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreePane extends VBox {

    private Accordion accordion = new Accordion();
    private Map<StringProperty, String> strings = new HashMap<>();
    private FXPath favoriteRoot;

    public FileTreePane(List<String> favoritePaths, Consumer<FXPath> fileSelectedHandler) {
        favoriteRoot = FXPath.getPseudoRoot(favoritePaths);
        accordion.getPanes().addAll(createFilesPane(fileSelectedHandler), createFavoritePane(fileSelectedHandler));
        accordion.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(accordion, Priority.ALWAYS);
        getChildren().add(accordion);
    }

    ObservableList<FXPath> getFavorites() {
        return favoriteRoot.getPaths();
    }

    private TitledPane createFilesPane(Consumer<FXPath> fileSelectedHandler) {

        PathTreeItem root = new PathTreeItem(FXPath.getRoot());

        FileTreeBox fileTree = new FileTreeBox(root, favoriteRoot, fileSelectedHandler);

        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Files");
        strings.put(pane.textProperty(), "files");
        pane.setContent(fileTree);

        return pane;
    }

    private TitledPane createFavoritePane(Consumer<FXPath> fileSelectedHandler) {


        PathTreeItem root = new PathTreeItem(favoriteRoot);

        FileTreeBox fileTree = new FileTreeBox(root, favoriteRoot, fileSelectedHandler);
        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        pane.setText("Favorites");
        strings.put(pane.textProperty(), "favotites");
        pane.setContent(fileTree);

        return pane;
    }
}

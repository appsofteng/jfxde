package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.function.Consumer;

import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.ui.PathTreeItem;
import javafx.collections.ObservableList;
import javafx.scene.control.Accordion;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FileTreePane extends VBox {

    private Accordion accordion = new Accordion();
    private FXPath favoriteRoot;

    public FileTreePane(List<String> favoritePaths, Consumer<List<FXPath>> fileSelectedHandler) {
        favoriteRoot = FXPath.getPseudoRoot(favoritePaths);
        accordion.getPanes().addAll(createRootPane(fileSelectedHandler), createFavoritePane(fileSelectedHandler));
        accordion.setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(accordion, Priority.ALWAYS);
        getChildren().add(accordion);
    }

    ObservableList<FXPath> getFavorites() {
        return favoriteRoot.getPaths();
    }

    private TitledPane createRootPane(Consumer<List<FXPath>> fileSelectedHandler) {

        PathTreeItem root = new PathTreeItem(FXPath.getRoot());

        FileTreeBox fileTree = new FileTreeBox(root, favoriteRoot, fileSelectedHandler);

        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        FXResourceBundle.getBundle().put(pane.textProperty(), "roots");
        pane.setContent(fileTree);

        return pane;
    }

    private TitledPane createFavoritePane(Consumer<List<FXPath>> fileSelectedHandler) {


        PathTreeItem root = new PathTreeItem(favoriteRoot);

        FileTreeBox fileTree = new FileTreeBox(root, favoriteRoot, fileSelectedHandler);
        TitledPane pane = new TitledPane();
        pane.setMinWidth(0);
        FXResourceBundle.getBundle().put(pane.textProperty(), "favorites");
        pane.setContent(fileTree);

        return pane;
    }
}

package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.ui.PathTreeItem;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class EditorContent extends BorderPane {

    private static final String FAVORITES = "favorites.json";

    private AppContext context;
    private EditorActions editorActions;
    private FileTreeBox fileTreeBox;
    private FXPath favorites;
    private EditorBar editorBar;
    private EditorPane editorPane;
    private SplitPane splitPane;
    private ReadOnlyBooleanWrapper stoppable = new ReadOnlyBooleanWrapper();
    private PathTreeItem rootItem;

    public EditorContent() {
    }

    EditorContent init(AppContext context) {
        this.context = context;
        List<String> favoritePaths = context.dc().fromJson(FAVORITES, List.class, List.of());

        FXPath roots = FXPath.getRoot();
        FXResourceBundle.getBundle().put(roots.nameProperty(), "roots");

        favorites = FXPath.getPseudoPath(favoritePaths);
        FXResourceBundle.getBundle().put(favorites.nameProperty(), "favorites");

        FXPath root = FXPath.getPseudoPath(roots, favorites);

        rootItem = new PathTreeItem(root);

        fileTreeBox = new FileTreeBox(rootItem, favorites, p -> editorPane.open(p));

        editorActions = new EditorActions(this);
        editorPane = new EditorPane(editorActions);
        editorActions.init();
        editorBar = new EditorBar(editorActions);

        splitPane = new SplitPane(fileTreeBox, editorPane);
        splitPane.setDividerPositions(0.3);
        SplitPane.setResizableWithParent(fileTreeBox, false);

        setListeners();

        setTop(editorBar);
        setCenter(splitPane);

        return this;
    }

    private void setListeners() {
        stoppable.bind(editorPane.changedProperty().not());

        favorites.getPaths().addListener((Change<? extends FXPath> c) -> {

            while (c.next()) {
                var favorites = c.getList().stream().map(p -> p.getPath().toString()).collect(Collectors.toList());
                context.dc().toJson(favorites, FAVORITES);
            }
        });
    }

    ReadOnlyBooleanProperty stoppableProperty() {
        return stoppable.getReadOnlyProperty();
    }

    EditorPane getEditorPane() {
        return editorPane;
    }

    void showInFavorites() {

        FXPath path = editorPane.getSelectedEditor().getPath();
        PathTreeItem favoriteItem = (PathTreeItem) rootItem.getChildren().stream().filter(i -> i.getValue().equals(favorites)).findFirst().get();

        favoriteItem.traverse(i -> {
            var result = false;

            if (i.getValue().equals(path)) {
                fileTreeBox.select(i);
            } else {
                result = path.getPath().startsWith(i.getValue().getPath());
            }

            return result;
        });
    }
}

package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.data.FXPath;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class EditorContent extends BorderPane {

    private static final String FAVORITES = "favorites.json";

    private AppContext context;
    private EditorActions editorActions;
    private FileTreePane fileTreePane;
    private EditorBar editorBar;
    private EditorPane editorPane = new EditorPane();
    private SplitPane splitPane;
    private ReadOnlyBooleanWrapper stoppable = new ReadOnlyBooleanWrapper();

    public EditorContent() {
    }

    EditorContent init(AppContext context) {
        this.context = context;
        List<String> favorites = context.dc().fromJson(FAVORITES, List.class, List.of());
        fileTreePane = new FileTreePane(favorites, p -> editorPane.setEditor(p));
        splitPane = new SplitPane(fileTreePane, editorPane);
        splitPane.setDividerPositions(0.3);
        SplitPane.setResizableWithParent(fileTreePane, false);

        editorActions = new EditorActions(this);
        editorBar = new EditorBar(editorActions);

        setListeners();

        setTop(editorBar);
        setCenter(splitPane);

        return this;
    }

    private void setListeners() {
        stoppable.bind(editorPane.changedProperty().not());

        fileTreePane.getFavorites().addListener((Change<? extends FXPath> c) -> {

            while (c.next()) {
                var favorites = c.getList().stream().map(p -> p.getPath().toString()).collect(Collectors.toList());
                context.dc().toJson(favorites, FAVORITES);
            }
        });
    }

    ReadOnlyBooleanProperty stoppableProperty() {
        return stoppable.getReadOnlyProperty();
    }

    public EditorPane getEditorPane() {
        return editorPane;
    }
}

package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.data.FXPath;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class EditorContent extends BorderPane {

    private AppContext context;
    private static final String FAVORITES = "favorites.json";
    private FileTreePane fileTreePane;
    private EditorBar editorBar = new EditorBar();
    private EditorPane editorPane = new EditorPane();
    private SplitPane splitPane;

    public EditorContent(AppContext context) {
        this.context = context;
        List<String> favorites = context.dc().fromJson(FAVORITES, List.class, List.of());
        fileTreePane = new FileTreePane(favorites, p -> editorPane.setEditor(p));
        splitPane = new SplitPane(fileTreePane, editorPane);
        splitPane.setDividerPositions(0.3);
        SplitPane.setResizableWithParent(fileTreePane, false);
        setTop(editorBar);
        setCenter(splitPane);
        setListsners();
    }

    private void setListsners() {
        fileTreePane.getFavorites().addListener((Change<? extends FXPath> c) -> {

            while (c.next()) {
                var favorites = c.getList().stream().map(p -> p.getPath().toString()).collect(Collectors.toList());
                context.dc().toJson(favorites, FAVORITES);
            }
        });
    }
}

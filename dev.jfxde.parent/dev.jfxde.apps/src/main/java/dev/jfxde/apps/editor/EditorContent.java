package dev.jfxde.apps.editor;

import java.util.List;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.util.JsonUtils;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.BorderPane;

public class EditorContent extends BorderPane {

    private static final String BOOKMARKS = "bookmarks.json";
    private FileTreePane fileTreePane;
    private EditorPane editorPane = new EditorPane();
    private SplitPane splitPane;

    public EditorContent(AppContext context) {

        List<String> bookmarks = JsonUtils.fromJson(context.fc().getAppDataDir().resolve(BOOKMARKS), List.class, List.of());
        fileTreePane = new FileTreePane(bookmarks);
        splitPane = new SplitPane(fileTreePane, editorPane);
        splitPane.setDividerPositions(0.2);
        SplitPane.setResizableWithParent(fileTreePane, false);
        setCenter(splitPane);
    }
}

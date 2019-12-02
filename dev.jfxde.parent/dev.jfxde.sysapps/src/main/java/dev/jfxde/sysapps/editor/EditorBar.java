package dev.jfxde.sysapps.editor;

import java.util.List;

import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.action.ActionUtils.ActionTextBehavior;

import javafx.scene.control.ToolBar;
import javafx.scene.layout.VBox;

public class EditorBar extends VBox {

    private EditorActions editorActions;

    public EditorBar(EditorActions editorActions) {
        this.editorActions = editorActions;
        getChildren().add(createToolBar());
    }

    private ToolBar createToolBar() {

        ToolBar toolBar = ActionUtils.createToolBar(List.of(editorActions.saveAction(), editorActions.saveAllAction()), ActionTextBehavior.HIDE);
        toolBar.getItems().forEach(n -> {
            n.setFocusTraversable(false);
            n.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        });

        return toolBar;
    }
}

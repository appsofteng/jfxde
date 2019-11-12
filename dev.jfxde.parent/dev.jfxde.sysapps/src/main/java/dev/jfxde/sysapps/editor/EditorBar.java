package dev.jfxde.sysapps.editor;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class EditorBar extends VBox {

    private EditorActions editorActions;

    public EditorBar(EditorActions editorActions) {
        this.editorActions = editorActions;
        getChildren().add(createToolBar());
    }

    private ToolBar createToolBar() {
        Button save = new Button(Fonts.Unicode.FLOPPY_DISK);
        save.setFocusTraversable(false);
        save.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        save.setTooltip(new Tooltip());
        FXResourceBundle.getBundle().put(save.getTooltip().textProperty(), "save");
        save.disableProperty().bind(editorActions.saveDisableProperty());
        save.setOnAction(editorActions::save);

        Button saveAll = new Button();
        saveAll.setFocusTraversable(false);
        saveAll.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        saveAll.setGraphic(new ImageView(getClass().getResource("save-all.png").toExternalForm()));
        saveAll.setTooltip(new Tooltip());
        FXResourceBundle.getBundle().put(saveAll.getTooltip().textProperty(), "saveAll");
        saveAll.disableProperty().bind(editorActions.saveAllDisableProperty());
        saveAll.setOnAction(editorActions::saveAll);

        ToolBar toolBar = new ToolBar(save, saveAll);

        return toolBar;
    }
}

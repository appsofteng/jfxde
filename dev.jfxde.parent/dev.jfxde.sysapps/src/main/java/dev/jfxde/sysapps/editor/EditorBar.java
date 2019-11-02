package dev.jfxde.sysapps.editor;

import dev.jfxde.fonts.Fonts;
import javafx.scene.control.Button;
import javafx.scene.control.ToolBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class EditorBar extends VBox {

    public EditorBar() {
        getChildren().add(createToolBar());
    }

    private ToolBar createToolBar() {
        Button save = new Button(Fonts.Unicode.FLOPPY_DISK);
        save.setFocusTraversable(false);
        save.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");

        Button saveAll = new Button();
        saveAll.setFocusTraversable(false);
        saveAll.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        saveAll.setGraphic(new ImageView(getClass().getResource("save-all.png").toExternalForm()));

        ToolBar toolBar = new ToolBar(save, saveAll);

        return toolBar;
    }
}

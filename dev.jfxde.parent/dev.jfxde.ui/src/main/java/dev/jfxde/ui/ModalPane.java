package dev.jfxde.ui;

import dev.jfxde.jfx.scene.control.InternalFrame;
import javafx.scene.layout.Pane;

public class ModalPane extends Pane {

    private DesktopEnvironment desktopEnvironment;

    public ModalPane(DesktopEnvironment desktopPane) {
        this.desktopEnvironment = desktopPane;
        setVisible(false);

        setOnMousePressed(e -> {
            if (e.getTarget() == this) {
                getChildren().stream().map(f -> (InternalFrame) f).forEach(f -> f.doModalEffect());
            }
        });
    }

    @Override
    public void toFront() {
        desktopEnvironment.setFreeze(true);
        super.toFront();
    }

    @Override
    public void toBack() {
        desktopEnvironment.setFreeze(false);
        super.toBack();
    }
}

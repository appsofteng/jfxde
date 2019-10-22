package dev.jfxde.ui;

import javafx.scene.layout.Pane;

public class ModalPane extends Pane {

    private DesktopPane desktopPane;

    public ModalPane(DesktopPane desktopPane) {
        this.desktopPane = desktopPane;
        setVisible(false);

        setOnMousePressed(e -> {
            if (e.getTarget() == this) {
                getChildren().stream().map(f -> (InternalFrame) f).forEach(f -> f.doModalEffect());
            }
        });
    }

    @Override
    public void toFront() {
        desktopPane.setFreeze(true);
        super.toFront();
    }

    @Override
    public void toBack() {
        desktopPane.setFreeze(false);
        super.toBack();
    }
}

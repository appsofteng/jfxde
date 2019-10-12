package dev.jfxde.ui;

import javafx.scene.layout.Region;

public abstract class InternalFrame extends Region {

    protected static final double CURSOR_BORDER_WIDTH = 5;

    abstract WindowPane getWindowPane();
    abstract void deactivate();
}

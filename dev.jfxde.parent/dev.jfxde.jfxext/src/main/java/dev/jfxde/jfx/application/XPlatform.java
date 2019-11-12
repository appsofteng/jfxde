package dev.jfxde.jfx.application;

import javafx.application.Platform;

public final class XPlatform {

    private XPlatform() {
    }

    public static void runFX(Runnable run) {
        if (Platform.isFxApplicationThread()) {
           run.run();
        } else {
            Platform.runLater(run);
        }
    }
}

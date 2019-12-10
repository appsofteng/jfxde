package dev.jfxde.ui;

import dev.jfxde.data.entity.Window;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import javafx.animation.PauseTransition;
import javafx.scene.control.ProgressIndicator;
import javafx.util.Duration;

public class AppWindow extends InternalWindow {

    private AppDescriptor appDescriptor;
    private ProgressIndicator progressIndicator;

    public AppWindow(WindowPane pane, Window window) {
        super(pane, window);
        this.appDescriptor = window.getContent();

        titleProperty().bind(appDescriptor.displayProperty());
        setIcon(appDescriptor.getAppProviderDescriptor().getSmallIcon());
        setIconSupplier(appDescriptor.getAppProviderDescriptor().getSmallIconSupplier());
        newWindow.setDisable(appDescriptor.isSingleton());

        setContentCss(appDescriptor.getAppProviderDescriptor().getCss());

        PauseTransition pause = new PauseTransition(Duration.seconds(1));

        appDescriptor.contentProperty().addListener((v, o, n) -> {
            if (n != null) {

                pause.stop();
                if (progressIndicator != null) {
                    removeContent();
                    progressIndicator = null;
                }

                setContent(n);
                closableProperty().bind(appDescriptor.getApp().stoppableProperty());

            }
        });

        pause.setOnFinished(w -> {
            progressIndicator = new ProgressIndicator();
            setContent(progressIndicator);
        });

        pause.play();
    }

    @Override
    protected void onNewWindow() {
        Sys.am().start(appDescriptor.getAppProviderDescriptor());
    }

    @Override
    protected void close() {
        super.close();
        Sys.am().stop(appDescriptor);
    }

    @Override
    protected void forceClose() {
        Sys.am().forceStop(appDescriptor);
    }
}

package dev.jfxde.ui;

import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.scene.control.AlertBuilder;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.FileManager;
import dev.jfxde.logic.Sys;
import javafx.application.Application;
import javafx.application.Preloader.ProgressNotification;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.KeyCombination;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Main extends Application {

    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {

        try {
            Files.createDirectories(FileManager.LOG_DIR);
            System.setProperty("javafx.preloader", MainPreloader.class.getName());
            launch(args);
        } catch (Throwable throwable) {
            LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
        }
    }

    @Override
    public void init() throws Exception {

        Sys.get().init(Main.class, (p) -> notifyPreloader(new ProgressNotification(p)));

        Fonts.getUrls().forEach((k, v) -> GlyphFontRegistry.register(k, v, 10));
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.setFullScreenExitKeyCombination(KeyCombination.NO_MATCH);
        stage.setFullScreenExitHint("");
        DesktopEnvironment desktopEnvironment = new DesktopEnvironment();

        Rectangle2D screen = Screen.getPrimary().getBounds();
        Scene scene = new Scene(desktopEnvironment, screen.getWidth() * 0.7 , screen.getHeight() * 0.7, false, SceneAntialiasing.BALANCED);
        scene.getStylesheets().add(Sys.rm().getCss("standard"));
        stage.setScene(scene);

        stage.setOnCloseRequest(e -> {
            if (!Sys.am().getAppDescriptors().isEmpty()) {

                var startedApps = Sys.am().getAppDescriptors().stream()
                        .map(p -> p.getDisplay())
                        .sorted()
                        .collect(Collectors.joining("\n"));

                AlertBuilder.get(desktopEnvironment.getModalPane(), AlertType.CONFIRMATION)
                .title(FXResourceBundle.getBundle().getString​("confirmation"))
                .headerText(FXResourceBundle.getBundle().getString​("someAppsStarted"))
                .contentText(FXResourceBundle.getBundle().getString​("stopApps"))
                .expandableTextArea(startedApps)
                .ok(() -> Sys.get().forceStop())
                .show();

                e.consume();
            }
        });

        stage.show();

        Sys.fm().watch(() -> {
            if (stage.isIconified()) {
                stage.setIconified(false);
            } else {
                stage.hide();
                stage.show();
            }
            // On Windows 7 toFront only flashes the icon on the taskbar therefore do hide
            // and show.
            stage.toFront();
            stage.requestFocus();
        });
    }

    @Override
    public void stop() throws Exception {
        Sys.get().stop();
    }
}

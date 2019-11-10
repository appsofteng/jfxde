package dev.jfxde.ui;

import dev.jfxde.data.entity.Desktop;
import dev.jfxde.data.entity.Window;
import dev.jfxde.data.entity.Window.State;
import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.scene.control.AlertBuilder;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.Sys;
import javafx.beans.binding.Bindings;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class DesktopEnvironment extends Region {

    private StackPane desktopStack = new StackPane();
    private MenuBar menuBar = new MenuBar();
    private ControlBar controlBar = new ControlBar();
    private DesktopPane activeDesktopPane;

    static final Duration SHOW_HIDE_DURATION = Duration.millis(800);
    private static final double DESKTOP_EXITED_Y_DIFF = 30;
    private double desktopStackExitedX;
    private double desktopStackExitedY;

    public DesktopEnvironment() {
        getChildren().addAll(desktopStack, menuBar, controlBar);
        setThemeColor(Sys.pm().getThemeColor());
        getStyleClass().add("jd-desktop-environment");
        setDesktopHandlers();
        setActiveDesktop();
        setDialogListener();
    }

    public void setThemeColor(String color) {

        String style = "-jd-base-color: " + color + ";";
        for (int i = 2; i < 8; i++) {
            style += "-jd-base-color-alpha" + i + ": " + Color.web(color, i / 10.0).toString().replace("0x", "#") + ";";
        }

        setStyle(style);
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(desktopStack, 0, 0, getWidth(), getHeight(), 0, new Insets(2, 2, 2, 2), HPos.CENTER, VPos.CENTER);
        layoutInArea(controlBar, -controlBar.getWidth() - 2, 0, 250, getHeight(), 0, new Insets(1, 0, 1, 2), HPos.LEFT,
                VPos.TOP);
        double menuBarWidth = menuBar.isDefaultMenuBar() ? menuBar.getWidth() : getWidth();

        layoutInArea(menuBar, getWidth() - menuBarWidth, -menuBar.getHeight() - 2, menuBarWidth, menuBar.getHeight(), 0,
                new Insets(2, 2, 0, 2), HPos.RIGHT, VPos.TOP);
    }

    private void setDialogListener() {
        Sys.am().toBeStartedApp().addListener((v, o, appProviderDescriptor) -> {
            if (appProviderDescriptor != null) {
                AlertBuilder.get(activeDesktopPane.getModalPane(), AlertType.CONFIRMATION)
                        .title(FXResourceBundle.getBundle().getString​("confirmation"))
                        .headerText(FXResourceBundle.getBundle(Main.class).getString("appPermissions", appProviderDescriptor.getName()))
                        .contentText(FXResourceBundle.getBundle(Main.class).getString("appPermissionConfirmation"))
                        .expandableContent(DataUtils.getAppPermissionTable(appProviderDescriptor))
                        .action(() -> Sys.am().allowAndStart(appProviderDescriptor))
                        .show();
            }
        });
    }

    private void setDesktopHandlers() {

        desktopStack.setOnMouseEntered(e -> {

            if (e.getX() >= 0 && e.getX() <= controlBar.getWidth()
                    && Math.abs(e.getY() - desktopStackExitedY) > DESKTOP_EXITED_Y_DIFF && desktopStackExitedX <= 0
                    && desktopStackExitedY > 0 && e.getY() > menuBar.getHeight()) {
                controlBar.show();
                menuBar.hide();
            }
        });

        desktopStack.setOnMouseExited(e -> {

            desktopStackExitedX = e.getX();
            desktopStackExitedY = e.getY();

            // menuBar.getWidth() cannot be used because it takes some time
            // before the width is changed after removal of app menu bar.
            if (e.getY() <= 0 && !menuBar.isDefaultMenuBar()
                    || e.getY() <= 0 && e.getX() >= desktopStack.getWidth() - menuBar.getButtonBox().getWidth()) {
                menuBar.show();
                controlBar.hide();

            } else if (e.getX() <= 0 && e.getY() > menuBar.getHeight()) {
                menuBar.hide();
            }
        });

        desktopStack.addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            controlBar.hide();
            menuBar.hide();
        });
    }

    private void setActiveDesktop() {
        setActiveDesktop(Sys.dm().getActiveDesktop());
        setFor(Sys.dm().getActiveWindow());

        Sys.dm().activeDesktopProperty().addListener((v, o, activeDesktop) -> {
            setActiveDesktop(activeDesktop);
        });

        Sys.dm().activeWindowProperty().addListener((v, o, w) -> {
            setFor(w);
        });
    }

    private void setActiveDesktop(Desktop activeDesktop) {

        activeDesktopPane = desktopStack.getChildren().stream().map(c -> (DesktopPane) c)
                .filter(d -> d.getDesktop() == activeDesktop).findFirst().orElse(null);

        if (activeDesktopPane == null) {
            activeDesktopPane = new DesktopPane(activeDesktop);
            desktopStack.getChildren().add(activeDesktopPane);
        }

        controlBar.bindDesktop(activeDesktopPane);

        activeDesktopPane.toFront();
    }

    private void setFor(Window window) {
        if (window != null) {
            menuBar.getRestore().textProperty().bind(Bindings.when(window.stateProperty().isEqualTo(State.FULL))
                    .then(Fonts.Octicons.SCREEN_NORMAL).otherwise(Fonts.Octicons.SCREEN_FULL));
            menuBar.getRestore().getTooltip().textProperty()
                    .bind(Bindings.when(window.stateProperty().isEqualTo(State.FULL))
                            .then(Sys.rm().getStringBinding("restore")).otherwise(Sys.rm().getStringBinding("full")));
            menuBar.getRestore().setDisable(false);
        } else {
            menuBar.setActiveAppMenuBar(null);
            menuBar.getRestore().textProperty().unbind();
            menuBar.getRestore().setText(Fonts.Octicons.SCREEN_FULL);
            menuBar.getRestore().getTooltip().textProperty().unbind();
            menuBar.getRestore().setDisable(true);
        }
    }
}

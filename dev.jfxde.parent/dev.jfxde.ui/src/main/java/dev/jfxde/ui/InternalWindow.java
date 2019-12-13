package dev.jfxde.ui;

import java.util.List;

import dev.jfxde.data.entity.Window;
import dev.jfxde.data.entity.Window.State;
import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.scene.control.InternalFrame;
import dev.jfxde.logic.Sys;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class InternalWindow extends InternalFrame {

    private static final PseudoClass TILED_PSEUDO_CLASS = PseudoClass.getPseudoClass("tiled");
    private static final PseudoClass FULL_PSEUDO_CLASS = PseudoClass.getPseudoClass("full");
    private static final Duration MINIMALIZATION_DURATION = Duration.millis(300);

    private WindowPane windowPane;
    private Window windowModel;

    protected Button newWindow;
    private Button tile;
    private Button minimize;
    private Button full;

    private ChangeListener<Boolean> activateListener = (v, o, n) -> {
        if (n) {
            activateAll();
        } else {
            deactivateAll();
        }
    };

    private ChangeListener<State> stateListener = (v, o, n) -> {

        pseudoClassStateChanged(TILED_PSEUDO_CLASS, n == State.TILED);
        pseudoClassStateChanged(FULL_PSEUDO_CLASS, n == State.FULL);

        if (o == State.RESTORED) {
            storeBounds();
        }

        if (n == State.MINIMIZED) {
            minimize();
        } else if (n == State.MAXIMIZED) {
            maximize();
        } else if (n == State.FULL) {
            full();
        } else if (n == State.RESTORED) {
            restore();
        } else if (n == State.TILED) {
            tile();
        } else if (n == State.CLOSED) {
            close();
        }

        setMaximized(n == State.MAXIMIZED);

        if (o == State.TILED) {
            windowPane.untile(this);
        }
    };

    public InternalWindow(WindowPane windowPane, Window windowModel) {
        super(windowPane);
        this.windowPane = windowPane;
        this.windowModel = windowModel;
        windowModel.activeProperty().addListener(activateListener);
        windowModel.stateProperty().addListener(stateListener);
        titleVisibleProperty().bind(windowModel.stateProperty().isNotEqualTo(State.FULL));

        setTitleMenu();
    }

    @Override
    public boolean isResizable() {
        return windowModel.isRestored();
    }

    public Window getWindow() {
        return windowModel;
    }

    protected void addButtons() {
        super.addButtons();
        newWindow = new Button(Fonts.Unicode.TWO_JOINED_SQUARES);
        newWindow.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        newWindow.setFocusTraversable(false);
        newWindow.setTooltip(new Tooltip());
        newWindow.getTooltip().textProperty().bind(Sys.rm().getStringBinding("newWindow"));

        tile = new Button(Fonts.FontAwesome.TH_LARGE);
        tile.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        tile.setFocusTraversable(false);
        tile.setTooltip(new Tooltip());
        tile.getTooltip().textProperty().bind(Sys.rm().getStringBinding("tile"));

        minimize = new Button("_");
        minimize.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        minimize.setFocusTraversable(false);
        minimize.setTooltip(new Tooltip());
        minimize.getTooltip().textProperty().bind(Sys.rm().getStringBinding("minimize"));

        full = new Button(Fonts.Octicons.SCREEN_FULL);
        full.getStyleClass().addAll("jd-frame-button", "jd-octicons");
        full.setFocusTraversable(false);
        full.setTooltip(new Tooltip());
        full.getTooltip().textProperty().bind(Sys.rm().getStringBinding("full"));

        addButtons(0, List.of(newWindow, tile, minimize));
        addButtons(-1, List.of(full));
    }

    private void setTitleMenu() {
        MenuItem minimizeOthers = new MenuItem();
        minimizeOthers.textProperty().bind(Sys.rm().getStringBinding("minimizeOthers"));
        minimizeOthers.disableProperty().bind(Bindings.size(windowPane.getVisibleWindows()).isEqualTo(1));
        minimizeOthers.setOnAction(e -> windowModel.getDesktop().minimizeOthers());

        MenuItem minimizeAll = new MenuItem();
        minimizeAll.textProperty().bind(Sys.rm().getStringBinding("minimizeAll"));
        minimizeAll.setOnAction(e -> windowModel.getDesktop().minimizeAll());

        MenuItem closeOthers = new MenuItem();
        closeOthers.textProperty().bind(Sys.rm().getStringBinding("closeOthers"));
        closeOthers.disableProperty().bind(Bindings.isEmpty(windowPane.getClosableWindows())
                .or(Bindings.size(windowPane.getClosableWindows()).isEqualTo(1).and(closableProperty())));
        closeOthers.setOnAction(e -> windowModel.getDesktop().closeOthers());

        MenuItem closeAll = new MenuItem();
        closeAll.textProperty().bind(Sys.rm().getStringBinding("closeAll"));
        closeAll.disableProperty().bind(Bindings.isEmpty(windowPane.getClosableWindows()));
        closeAll.setOnAction(e -> windowModel.getDesktop().closeAll());

        MenuItem forceClose = new MenuItem();
        forceClose.textProperty().bind(Sys.rm().getStringBinding("forceClose"));
        forceClose.setOnAction(e -> forceClose());

        ContextMenu titleContextMenu = new ContextMenu(minimizeOthers, minimizeAll, closeOthers, closeAll, new SeparatorMenuItem(), forceClose);
        setTitleContextMenu(titleContextMenu);
    }

    protected boolean isEnlarged() {
        return windowModel.isMaximized() || windowModel.isTiled();
    }

    protected void onRestore() {
        windowModel.restore();
    }

    @Override
    protected void setHandlers() {
        super.setHandlers();
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            InternalFrame modalFrame = getModalFrame(this);

            if (windowModel.isActive()) {

                if (modalFrame != null) {
                    modalFrame.doModalEffect();
                } else {
                    activate();
                }
            } else {
                windowModel.activate();
                if (modalFrame != null) {
                    modalFrame.doModalEffect();
                }
            }
        });

        newWindow.setOnAction(e -> onNewWindow());
        tile.setOnAction(e -> windowPane.tile());
        minimize.setOnAction(e -> windowModel.minimizeActivate());
        full.setOnAction(e -> windowModel.full());
    }

    protected void onNewWindow() {

    }

    protected void onClose() {
        windowModel.close();
    }

    protected void forceClose() {
    }

    @Override
    protected void onMaximizeRestore() {
        windowModel.maximizeRestore();
    }

    protected void activateRoot() {
        windowModel.activate();
    }

    public void activate() {
        setActive(true);
        getSubframes().forEach(InternalFrame::deactivateAll);
        requestFocus();
    }

    void activateAll() {
        toFront();
        InternalFrame modalFrame = getModalFrame(this);
        if (modalFrame != null) {
            modalFrame.activate();
        } else {

            setActive(true);

            if (windowModel.isMinimized()) {
                restoreTransition(MINIMALIZATION_DURATION);
            }

            requestFocus();
        }
    }

    void minimize() {
        setSubFramesVisible(false);

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        getPayload().prefWidthProperty().unbind();
        getPayload().prefHeightProperty().unbind();

        minimizeTransition(MINIMALIZATION_DURATION);
    }

    private void full() {

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        getPayload().prefWidthProperty().unbind();
        getPayload().prefHeightProperty().unbind();
        setLayoutX(0);
        setLayoutY(0);

        getPayload().prefWidthProperty().bind(windowPane.widthProperty());
        getPayload().prefHeightProperty().bind(windowPane.heightProperty());

        toFront();
    }

    void tile() {

        windowPane.tile(this);

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        getPayload().prefWidthProperty().unbind();
        getPayload().prefHeightProperty().unbind();

        layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> windowPane.getTiledWindows().indexOf(this) % windowPane.tileColsProperty().get()
                        * windowPane.tileWidthProperty().get(),
                windowPane.tileColsProperty(), windowPane.tileWidthProperty()));
        layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> windowPane.getTiledWindows().indexOf(this) / windowPane.tileColsProperty().get()
                        * windowPane.tileHeightProperty().get(),
                windowPane.tileColsProperty(), windowPane.tileHeightProperty()));

        getPayload().prefWidthProperty().bind(windowPane.tileWidthProperty());
        getPayload().prefHeightProperty().bind(windowPane.tileHeightProperty());
    }

    private void minimizeTransition(Duration duration) {

        ParallelTransition parallelTransition = new ParallelTransition();
        parallelTransition.setOnFinished(e -> setVisible(false));

        TranslateTransition translateTransition = new TranslateTransition(duration, this);
        float scaleFactor = 0.1f;
        translateTransition.setToX(-getLayoutX() - (getWidth() - scaleFactor * getWidth()) / 2);
        translateTransition
                .setToY(windowPane.getHeight() / 2 - getLayoutY() - (getHeight() - getHeight() * scaleFactor) / 2);

        ScaleTransition scaleTransition = new ScaleTransition(duration, this);
        scaleTransition.setToX(scaleFactor);
        scaleTransition.setToY(scaleFactor);

        FadeTransition fadeTransition = new FadeTransition(duration, this);
        fadeTransition.setToValue(0);

        parallelTransition.getChildren().addAll(translateTransition, scaleTransition, fadeTransition);

        parallelTransition.play();
    }

    private void restoreTransition(Duration duration) {

        setVisible(true);
        ParallelTransition parallelTransition = new ParallelTransition();

        TranslateTransition translateTransition = new TranslateTransition(duration, this);
        translateTransition.setToX(0);
        translateTransition.setToY(0);

        ScaleTransition scaleTransition = new ScaleTransition(duration, this);
        scaleTransition.setToX(1);
        scaleTransition.setToY(1);

        FadeTransition fadeTransition = new FadeTransition(duration, this);
        fadeTransition.setToValue(1);

        parallelTransition.getChildren().addAll(translateTransition, scaleTransition, fadeTransition);
        parallelTransition.setOnFinished(e -> setSubFramesVisible(true));

        parallelTransition.play();
    }

    public void dispose() {
        windowModel.activeProperty().removeListener(activateListener);
        windowModel.stateProperty().removeListener(stateListener);
    }
}

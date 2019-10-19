package dev.jfxde.ui;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfxext.util.LayoutUtils;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.Window;
import dev.jfxde.logic.data.Window.State;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;

public class InternalWindow extends InternalFrame {

    private static final PseudoClass FULL_PSEUDO_CLASS = PseudoClass.getPseudoClass("full");
    private static final Duration MINIMALIZATION_DURATION = Duration.millis(300);

    private Window windowModel;

    protected Button newWindow = new Button(Fonts.Unicode.TWO_JOINED_SQUARES);
    private Button tile = new Button(Fonts.FontAwesome.TH_LARGE);
    private Button minimize = new Button("_");
    private Button maximize = new Button(Fonts.Unicode.WHITE_LARGE_SQUARE);
    private Button full = new Button(Fonts.Octicons.SCREEN_FULL);

    private ObservableList<InternalDialog> dialogs = FXCollections.observableArrayList();
    private ObservableList<InternalDialog> modalDialogs = FXCollections.observableArrayList();;

    private ChangeListener<Boolean> activateListener = (v, o, n) -> {
        if (n) {
            activateAll();
        } else {
            deactivate();
            dialogs.forEach(InternalDialog::deactivate);
        }
    };

    private ChangeListener<State> stateListener = (v, o, n) -> {

        pseudoClassStateChanged(FULL_PSEUDO_CLASS, n == State.FULL);

        if (n == State.MINIMIZED) {
            minimize(o);
        } else if (n == State.MAXIMIZED) {
            maximize(o);
        } else if (n == State.FULL) {
            full(o);
        } else if (n == State.RESTORED) {
            restore(o);
        } else if (n == State.TILED) {
            tile(o);
        } else if (n == State.CLOSED) {
            close();
        }

        if (o == State.TILED) {
            windowPane.untile(this);
        }
    };

    public InternalWindow(Window windowModel, WindowPane windowPane) {
        super(windowPane);
        this.windowModel = windowModel;
        this.window = this;

        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
        setTitleMenu();
    }

    void setCss(String css) {
        if (css != null) {
            contentRegion.getStylesheets().addAll(css);
        }
    }

    void add(InternalDialog dialog) {
        dialogs.add(dialog);
        if (dialog.isModal()) {
            modalDialogs.add(dialog);
        }
    }

    void remove(InternalDialog dialog) {
        dialogs.remove(dialog);
        modalDialogs.remove(dialog);
    }

    public ObservableList<InternalDialog> getDialogs() {
        return dialogs;
    }

    public ObservableList<InternalDialog> getModalDialogs() {
        return modalDialogs;
    }

    @Override
    public boolean isResizable() {
        return windowModel.isRestored();
    }

    public Window getWindow() {
        return windowModel;
    }

    protected void buildLayout(double width, double height) {
        super.buildLayout(width, height);
        titleBar.visibleProperty().bind(windowModel.stateProperty().isNotEqualTo(State.FULL));
        titleBar.managedProperty().bind(titleBar.visibleProperty());
    }

    protected void addButtons() {
        super.addButtons();
        newWindow.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        newWindow.setFocusTraversable(false);
        newWindow.setTooltip(new Tooltip());
        newWindow.getTooltip().textProperty().bind(Sys.rm().getStringBinding("newWindow"));

        tile.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        tile.setFocusTraversable(false);
        tile.setTooltip(new Tooltip());
        tile.getTooltip().textProperty().bind(Sys.rm().getStringBinding("tile"));

        minimize.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        minimize.setFocusTraversable(false);
        minimize.setTooltip(new Tooltip());
        minimize.getTooltip().textProperty().bind(Sys.rm().getStringBinding("minimize"));

        maximize.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        maximize.setFocusTraversable(false);
        maximize.textProperty()
                .bind(Bindings.when(windowModel.stateProperty().isEqualTo(State.MAXIMIZED))
                        .then(Fonts.Unicode.UPPER_RIGHT_DROP_SHADOWED_WHITE_SQUARE)
                        .otherwise(Fonts.Unicode.WHITE_LARGE_SQUARE));
        maximize.setTooltip(new Tooltip());
        maximize.getTooltip().textProperty().bind(Bindings.when(windowModel.stateProperty().isEqualTo(State.MAXIMIZED))
                .then(Sys.rm().getStringBinding("restore")).otherwise(Sys.rm().getStringBinding("maximize")));

        full.getStyleClass().addAll("jd-frame-button", "jd-octicons");
        full.setFocusTraversable(false);
        full.setTooltip(new Tooltip());
        full.getTooltip().textProperty().bind(Sys.rm().getStringBinding("full"));

        buttonBox.getChildren().addAll(newWindow, tile, minimize, maximize, full, close);
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
                .or(Bindings.size(windowPane.getClosableWindows()).lessThan(2)));
        closeOthers.setOnAction(e -> windowModel.getDesktop().closeOthers());

        MenuItem closeAll = new MenuItem();
        closeAll.textProperty().bind(Sys.rm().getStringBinding("closeAll"));
        closeAll.disableProperty().bind(Bindings.isEmpty(windowPane.getClosableWindows()));
        closeAll.setOnAction(e -> windowModel.getDesktop().closeAll());

        ContextMenu titleContextMenu = new ContextMenu(minimizeOthers, minimizeAll, closeOthers, closeAll);
        titleLabel.setContextMenu(titleContextMenu);
    }

    private void setMoveable() {
        LayoutUtils.makeDragable(this, titleBar, e -> {
            if (windowModel.isMaximized() || windowModel.isTiled()) {
                Point2D localClickPoint = windowPane.screenToLocal(e.getScreenX(), e.getScreenY());
                double restoreX = localClickPoint.getX() - restoreBounds.getWidth() / 2;
                restoreX = Math.max(0, restoreX);
                restoreX = Math.min(windowPane.getWidth() - restoreBounds.getWidth(), restoreX);
                double restoreY = localClickPoint.getY() - e.getY();
                pressDragPoint = new Point2D(restoreX, restoreY);
            } else {
                pressDragPoint = new Point2D(getLayoutX(), getLayoutY());
            }

            return pressDragPoint;
        }, () -> {

            if (windowModel.isMaximized() || windowModel.isTiled()) {
                restoreBounds = new BoundingBox(pressDragPoint.getX(), 0, restoreBounds.getWidth(),
                        restoreBounds.getHeight());
                windowModel.restore();
            }
        });

        LayoutUtils.makeResizable(this, payload, CURSOR_BORDER_WIDTH);
    }

    private void setHandlers() {
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {

            InternalDialog dialog = getModalDialog();

            if (dialog != null) {
                dialog.doModalEffect();
            }

            if (windowModel.isActive()) {
                if (modalDialogs.isEmpty()) {
                    activate();
                }
            } else {
                windowModel.activate();
            }
        });

        titleLabel.setOnMouseClicked(e -> {

            if (modalDialogs.isEmpty()) {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    windowModel.maximizeRestore();
                }
            } else {
                e.consume();
            }

        });

        newWindow.setOnAction(e -> onNewWindow());
        tile.setOnAction(e -> {
            windowPane.tile();
        });
        minimize.setOnAction(e -> windowModel.minimizeActivate());
        maximize.setOnAction(e -> windowModel.maximizeRestore());
        full.setOnAction(e -> windowModel.full());

        close.setOnAction(e -> windowModel.close());

        windowModel.activeProperty().addListener(activateListener);

        windowModel.stateProperty().addListener(stateListener);
    }

    protected void onNewWindow() {

    }

    private void close() {
        windowPane.getChildren().removeAll(dialogs);
        onClose();
    }

    protected void onClose() {
    }

    InternalDialog getModalDialog() {
        var modalDialog = modalDialogs.isEmpty() ? null : modalDialogs.get(modalDialogs.size() - 1);
        return modalDialog;
    }

    void activateWindow() {
        windowModel.activate();
    }

    void activate() {
        active.set(true);
        deactivateDialogs();
        requestFocus();
        focusOwner.requestFocus();
    }

    void activateAll() {
        toFront();

        if (!modalDialogs.isEmpty()) {
            getModalDialog().activate();
        } else {

            active.set(true);

            if (windowModel.isMinimized()) {
                restoreTransition(MINIMALIZATION_DURATION);
            }

            focusOwner.requestFocus();
        }
    }

    void deactivateDialogs() {
        dialogs.forEach(InternalDialog::deactivate);
    }

    void minimize(State old) {
        dialogs.forEach(d -> d.setVisible(false));
        if (old == State.RESTORED) {
            restoreBounds = getBoundsInParent();
        }

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();

        minimizeTransition(MINIMALIZATION_DURATION);
    }

    private void maximize(State old) {

        if (old == State.RESTORED) {
            restoreBounds = getBoundsInParent();
        }

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        setLayoutX(0);
        setLayoutY(0);

        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();
        payload.prefWidthProperty().bind(windowPane.widthProperty());
        payload.prefHeightProperty().bind(windowPane.heightProperty());

        toFront();
    }

    private void full(State old) {

        if (old == State.RESTORED) {
            restoreBounds = getBoundsInParent();
        }

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();
        setLayoutX(0);
        setLayoutY(0);

        payload.prefWidthProperty().bind(windowPane.widthProperty());
        payload.prefHeightProperty().bind(windowPane.heightProperty());

        toFront();
    }

    void tile(State old) {

        if (old == State.RESTORED) {
            restoreBounds = getBoundsInParent();
        }

        windowPane.tile(this);

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();

        layoutXProperty().bind(Bindings.createDoubleBinding(
                () -> windowPane.getTiledWindows().indexOf(this) % windowPane.tileColsProperty().get()
                        * windowPane.tileWidthProperty().get(),
                windowPane.tileColsProperty(), windowPane.tileWidthProperty()));
        layoutYProperty().bind(Bindings.createDoubleBinding(
                () -> windowPane.getTiledWindows().indexOf(this) / windowPane.tileColsProperty().get()
                        * windowPane.tileHeightProperty().get(),
                windowPane.tileColsProperty(), windowPane.tileHeightProperty()));

        payload.prefWidthProperty().bind(windowPane.tileWidthProperty());
        payload.prefHeightProperty().bind(windowPane.tileHeightProperty());
    }

    void restore(State old) {
        layoutXProperty().unbind();
        layoutYProperty().unbind();
        setLayoutX(restoreBounds.getMinX());
        setLayoutY(restoreBounds.getMinY());

        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();
        payload.setPrefSize(restoreBounds.getWidth(), restoreBounds.getHeight());
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
        parallelTransition.setOnFinished(e -> dialogs.forEach(d -> d.setVisible(true)));

        parallelTransition.play();
    }

    public void dispose() {
        windowModel.activeProperty().removeListener(activateListener);
        windowModel.stateProperty().removeListener(stateListener);
    }
}

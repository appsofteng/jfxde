package dev.jfxde.ui;

import dev.jfxde.api.ui.Fonts;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.Window;
import dev.jfxde.logic.data.Window.State;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.css.PseudoClass;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import javafx.scene.Node;

public class InternalWindow extends Pane {

    private static final double CURSOR_BORDER_WIDTH = 5;
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");
    private static final PseudoClass FULL_PSEUDO_CLASS = PseudoClass.getPseudoClass("full");

    private Window window;
    private WindowPane windowPane;
    protected Label title = new Label();
    private HBox buttonBox = new HBox();
    protected Button newWindow = new Button(Fonts.Unicode.TWO_JOINED_SQUARES);
    private Button tile = new Button(Fonts.FontAwesome.TH_LARGE);
    private Button minimize = new Button("_");
    private Button maximize = new Button(Fonts.Unicode.WHITE_LARGE_SQUARE);
    private Button full = new Button(Fonts.Octicons.SCREEN_FULL);
    private Button close = new Button("x");

    private BorderPane titleBar = new BorderPane();
    private BorderPane payload = new BorderPane();
    private StackPane contentPane = new StackPane();

    private static final Duration MINIMALIZATION_DURATION = Duration.millis(300);

    private Bounds restoreBounds;
    private Point2D pressDragPoint;
    private Node focusOwner = contentPane;

    private ChangeListener<Boolean> activateListener = (v, o, n) -> {
        if (n) {
            activate();
        } else {
            deactivate();
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
            onClose();
        }

        if (o == State.TILED) {
            windowPane.untile(this);
        }
    };

    public InternalWindow(Window window, WindowPane windowPane) {
        this.window = window;
        this.windowPane = windowPane;
        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
        setTitleMenu();
    }

    void setContent(Node node) {
        contentPane.getChildren().add(node);
        focusOwner = node;
    }

    @Override
    public boolean isResizable() {
        return window.isRestored();
    }

    public Window getWindow() {
        return window;
    }

    private void buildLayout(double width, double height) {
        title.setPrefWidth(Double.MAX_VALUE);

        titleBar.setLeft(title);
        titleBar.setRight(buttonBox);
        titleBar.minWidthProperty().bind(buttonBox.widthProperty().add(10));
        titleBar.visibleProperty().bind(window.stateProperty().isNotEqualTo(State.FULL));
        titleBar.managedProperty().bind(titleBar.visibleProperty());

        payload.setTop(titleBar);
        payload.setCenter(contentPane);

        titleBar.getStyleClass().add("jd-internal-window-title-bar");
        contentPane.getStyleClass().add("jd-internal-window-content");
        payload.getStyleClass().add("jd-internal-window-payload");
        getStyleClass().add("jd-internal-window");
        payload.minWidthProperty().bind(titleBar.minWidthProperty().add(10));

        payload.setPrefSize(width, height);
        getChildren().add(payload);
        relocate(width / 2, height / 2);
        restoreBounds = getBoundsInParent();
    }

    private void addButtons() {
        newWindow.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        newWindow.setFocusTraversable(false);
        newWindow.setTooltip(new Tooltip());
        newWindow.getTooltip().textProperty().bind(Sys.rm().getTextBinding("newWindow"));

        tile.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        tile.setFocusTraversable(false);
        tile.setTooltip(new Tooltip());
        tile.getTooltip().textProperty().bind(Sys.rm().getTextBinding("tile"));

        minimize.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        minimize.setFocusTraversable(false);
        minimize.setTooltip(new Tooltip());
        minimize.getTooltip().textProperty().bind(Sys.rm().getTextBinding("minimize"));

        maximize.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        maximize.setFocusTraversable(false);
        maximize.textProperty()
                .bind(Bindings.when(window.stateProperty().isEqualTo(State.MAXIMIZED))
                        .then(Fonts.Unicode.UPPER_RIGHT_DROP_SHADOWED_WHITE_SQUARE)
                        .otherwise(Fonts.Unicode.WHITE_LARGE_SQUARE));
        maximize.setTooltip(new Tooltip());
        maximize.getTooltip().textProperty().bind(Bindings.when(window.stateProperty().isEqualTo(State.MAXIMIZED))
                .then(Sys.rm().getTextBinding("restore")).otherwise(Sys.rm().getTextBinding("maximize")));

        full.getStyleClass().addAll("jd-internal-window-button", "jd-octicons");
        full.setFocusTraversable(false);
        full.setTooltip(new Tooltip());
        full.getTooltip().textProperty().bind(Sys.rm().getTextBinding("full"));

        close.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        close.getTooltip().textProperty().bind(Sys.rm().getTextBinding("close"));

        buttonBox.getChildren().addAll(newWindow, tile, minimize, maximize, full, close);
        buttonBox.setMinWidth(USE_PREF_SIZE);
    }

    private void setTitleMenu() {
        MenuItem minimizeOthers = new MenuItem();
        minimizeOthers.textProperty().bind(Sys.rm().getTextBinding("minimizeOthers"));
        minimizeOthers.disableProperty().bind(Bindings.size(windowPane.getVisibleWindows()).isEqualTo(1));
        minimizeOthers.setOnAction(e -> window.getDesktop().minimizeOthers());

        MenuItem minimizeAll = new MenuItem();
        minimizeAll.textProperty().bind(Sys.rm().getTextBinding("minimizeAll"));
        minimizeAll.setOnAction(e -> window.getDesktop().minimizeAll());

        MenuItem closeOthers = new MenuItem();
        closeOthers.textProperty().bind(Sys.rm().getTextBinding("closeOthers"));
        closeOthers.disableProperty().bind(Bindings.isEmpty(windowPane.getClosableWindows())
                .or(Bindings.size(windowPane.getClosableWindows()).lessThan(2)));
        closeOthers.setOnAction(e -> window.getDesktop().closeOthers());

        MenuItem closeAll = new MenuItem();
        closeAll.textProperty().bind(Sys.rm().getTextBinding("closeAll"));
        closeAll.disableProperty().bind(Bindings.isEmpty(windowPane.getClosableWindows()));
        closeAll.setOnAction(e -> window.getDesktop().closeAll());

        ContextMenu titleContextMenu = new ContextMenu(minimizeOthers, minimizeAll, closeOthers, closeAll);
        title.setContextMenu(titleContextMenu);
    }

    private void setMoveable() {
        LayoutUtils.makeDragable(this, titleBar, e -> {
            if (window.isMaximized() || window.isTiled()) {
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

            if (window.isMaximized() || window.isTiled()) {
                restoreBounds = new BoundingBox(pressDragPoint.getX(), 0, restoreBounds.getWidth(),
                        restoreBounds.getHeight());
                window.restore();
            }
        });

        LayoutUtils.makeResizable(this, payload, CURSOR_BORDER_WIDTH);
    }

    private void setHandlers() {
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            window.activate();
        });

        title.setOnMouseClicked(e -> {

            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                window.maximizeRestore();
            }
        });

        newWindow.setOnAction(e -> onNewWindow());
        tile.setOnAction(e -> {
            windowPane.tile();
        });
        minimize.setOnAction(e -> window.minimizeActivate());
        maximize.setOnAction(e -> window.maximizeRestore());
        full.setOnAction(e -> window.full());

        close.setOnAction(e -> window.close());

        window.activeProperty().addListener(activateListener);

        window.stateProperty().addListener(stateListener);
    }

    protected void onNewWindow() {

    }

    protected void onClose() {
    }

    void activate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);

        if (window.isMinimized()) {
            restoreTransition(MINIMALIZATION_DURATION);
        }

        toFront();

        focusOwner.requestFocus();
    }

    void deactivate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
        focusOwner = getScene().getFocusOwner();
    }

    void minimize(State old) {

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

        parallelTransition.play();
    }

    public void dispose() {
        window.activeProperty().removeListener(activateListener);
        window.stateProperty().removeListener(stateListener);
    }
}

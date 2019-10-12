package dev.jfxde.ui;

import dev.jfxde.jfxext.util.LayoutUtils;
import dev.jfxde.logic.Sys;
import javafx.collections.ListChangeListener.Change;
import javafx.css.PseudoClass;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

public class InternalDialog extends InternalFrame {

    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    private InternalWindow window;
    private WindowPane windowPane;
    private Label title = new Label();
    private HBox buttonBox = new HBox();
    private BorderPane titleBar = new BorderPane();
    private BorderPane payload = new BorderPane();
    private StackPane contentPane = new StackPane();
    private Node focusOwner = contentPane;

    private Button close = new Button("x");

    private Point2D pressDragPoint;
    private Bounds restoreBounds;

    private int index;

    private InternalDialog(InternalWindow window) {
        this.window = window;
        window.getDialogs().add(this);
        this.windowPane = window.getWindowPane();
        addButtons();
        buildLayout(window.getBoundsInLocal().getWidth() / 2, window.getBoundsInLocal().getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    @Override
    WindowPane getWindowPane() {
        return windowPane;
    }

    private void addButtons() {
        close.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        close.getTooltip().textProperty().bind(Sys.rm().getStringBinding("close"));

        buttonBox.getChildren().addAll(close);
        buttonBox.setMinWidth(USE_PREF_SIZE);
        buttonBox.setMinHeight(USE_PREF_SIZE);
    }

    private void buildLayout(double width, double height) {
        payload.setPrefSize(width, height);

        title.setPrefWidth(Double.MAX_VALUE);

        titleBar.setLeft(title);
        titleBar.setRight(buttonBox);
        titleBar.minWidthProperty().bind(buttonBox.widthProperty().add(10));
        payload.setTop(titleBar);
        payload.setCenter(contentPane);
        payload.minWidthProperty().bind(titleBar.minWidthProperty().add(10));
        payload.setMinHeight(70);

        titleBar.getStyleClass().add("jd-internal-window-title-bar");
        contentPane.getStyleClass().add("jd-internal-window-content");
        payload.getStyleClass().add("jd-internal-window-payload");
        getStyleClass().add("jd-internal-window");

        getChildren().add(payload);
        relocate(width / 2 + window.getLayoutX(), height / 2 + window.getLayoutY());
        restoreBounds = getBoundsInParent();
    }

    private void setMoveable() {
        LayoutUtils.makeDragable(this, titleBar, e -> {
            if (isMaximized()) {
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

            if (isMaximized()) {
                restoreBounds = new BoundingBox(pressDragPoint.getX(), 0, restoreBounds.getWidth(),
                        restoreBounds.getHeight());
                restore();
            }
        });

        LayoutUtils.makeResizable(this, payload, CURSOR_BORDER_WIDTH);
    }

    private void setHandlers() {
        addEventFilter(MouseEvent.MOUSE_PRESSED, e -> {
            activate();
        });

        windowPane.getChildren().addListener((Change<? extends Node> c) -> {

            while (c.next()) {
                if (c.wasAdded()) {
                    if (windowPane.getChildren().indexOf(this) == windowPane.getChildren().size() - 1) {
                        activate();
                    }
                } else if (c.wasRemoved()) {
                    if (c.getRemoved().contains(this) && c.getFrom() == c.getList().size()) {
                        deactivate();
                    }
                }
            }
        });

        close.setOnAction(e -> close());
    }

    private void close() {
        setVisible(false);
        window.getDialogs().remove(this);
        window.getWindowPane().getChildren().remove(this);
    }

    private boolean isMaximized() {
        return false;
    }

    private void restore() {

    }

    public static InternalDialog create(Node node) {
        InternalDialog dialog = null;
        Parent parent = node.getParent();

        while (parent != null && !(parent instanceof InternalWindow)) {
            parent = parent.getParent();
        }

        if (parent instanceof InternalWindow) {
            dialog = new InternalDialog((InternalWindow) parent);
        }
        return dialog;
    }

    public void show() {

        window.getWindowPane().getChildren().add(this);
        index = window.getWindowPane().getChildren().size() - 1;
        activate();
    }

    void activate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);

        toFront();

        focusOwner.requestFocus();
    }

    void deactivate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
        focusOwner = getScene().getFocusOwner();
    }
}

package dev.jfxde.ui;

import dev.jfxde.jfxext.util.LayoutUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.MouseEvent;

public class InternalDialog extends InternalFrame {

    private InternalWindow window;
    private boolean modal;

    private InternalDialog(InternalWindow window) {
        this(window, false);
    }

    private InternalDialog(InternalWindow window, boolean modal) {
        super(window.getWindowPane());
        this.window = window;
        this.parent = window;
        this.modal = modal;
        if (modal) {
            parent.freez();
        }
        window.add(this);
        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    protected void addButtons() {
       super.addButtons();

        buttonBox.getChildren().addAll(close);
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

            if (!isActive()) {

                activateAll();
            }
        });

        close.setOnAction(e -> close());
    }

    private boolean isMaximized() {
        return false;
    }

    private void restore() {

    }

    public boolean isModal() {
        return modal;
    }

    void disableOthers() {

    }

    public static InternalDialog create(Node node) {
        return create(node, false);
    }

    public static InternalDialog create(Node node, boolean modal) {
        InternalDialog dialog = null;
        Parent parent = node.getParent();

        while (parent != null && !(parent instanceof InternalWindow)) {
            parent = parent.getParent();
        }

        if (parent instanceof InternalWindow) {
            dialog = new InternalDialog((InternalWindow) parent, modal);
        }
        return dialog;
    }

    public void show(Node node) {
        setContent(node);
        payload.setPrefWidth(USE_COMPUTED_SIZE);
        payload.setPrefHeight(USE_COMPUTED_SIZE);

        payload.heightProperty().addListener((v,o,n) -> {
            if (payload.getPrefHeight() == USE_COMPUTED_SIZE) {
                payload.setPrefHeight(Math.min(n.doubleValue(), windowPane.getHeight() - 20));
                payload.setPrefWidth(windowPane.getWidth() / 2);
                center();
            }
        });

        window.getWindowPane().getChildren().add(this);
        activateAll();
    }

    public void close() {
        window.remove(this);
        window.getDialogs().removeAll(subdialogs);
        window.getWindowPane().getChildren().remove(this);
        window.getWindowPane().getChildren().removeAll(subdialogs);
        if (modal) {
            parent.unfreez();
        }
        parent.activate();
    }

    void activateAll() {
        window.activateWindow();
        var modalDialog = window.getModalDialog();

        if (modalDialog == null || modalDialog == this) {
            window.deactivate();
            window.deactivateDialogs();
            active.set(true);
            toFront();
            focusOwner.requestFocus();
        }
    }

    void activate() {
        active.set(true);
        toFront();
        focusOwner.requestFocus();
    }
}

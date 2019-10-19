package dev.jfxde.ui;

import dev.jfxde.jfxext.util.LayoutUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;

public class InternalDialog extends InternalFrame {

    private boolean modal;

    public InternalDialog(Node node) {
        this(node, false);
    }

    public InternalDialog(Node node, boolean modal) {
        this(findParent(node), modal);
    }

    private InternalDialog(InternalFrame frame, boolean modal) {
        super(frame.getWindowPane());
        this.window = frame.window;
        this.parent = frame;
        this.modal = modal;
        if (modal) {
            parent.freez();
        }
        parent.subdialogs.add(this);
        window.add(this);
        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    private static InternalFrame findParent(Node node) {

        InternalFrame frame = null;
        Parent parent = node.getParent();

        while (parent != null && !(parent instanceof InternalFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof InternalFrame) {
            frame = (InternalFrame) parent;
        }

        return frame;
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

            InternalDialog dialog = window.getModalDialog();

            if (dialog != null && dialog != this) {
                dialog.doModalEffect();
            }

            if (!isActive()) {

                activateAll();
            }
        });

        addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.ESCAPE) {
                close();
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

    public void show(Node node) {
        setContent(node);
        payload.setPrefWidth(windowPane.getWidth() / 2);
        payload.setPrefHeight(USE_COMPUTED_SIZE);

        payload.heightProperty().addListener((v, o, n) -> {
            if (payload.getPrefHeight() == USE_COMPUTED_SIZE) {
                payload.setPrefHeight(Math.min(n.doubleValue(), windowPane.getHeight() - 20));
                center();
                setVisible(true);
            }
        });

        setVisible(false);
        windowPane.getChildren().add(this);
        activateAll();
    }

    public void close() {
        parent.subdialogs.remove(this);
        window.remove(this);
        window.getWindowPane().getChildren().remove(this);
        subdialogs.forEach(InternalDialog::close);
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

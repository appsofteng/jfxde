package dev.jfxde.ui;

import dev.jfxde.jfxext.util.LayoutUtils;
import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;

public class InternalDialog extends InternalFrame {

    public InternalDialog(Node node) {
        this(node, Modality.NONE);
    }

    public InternalDialog(Node node, Modality modality) {
        this(findParent(node), modality);
    }

    private InternalDialog(InternalFrame parent, Modality modality) {
        super(parent.windowPane);
        this.parent = parent;
        this.modality = modality;

        parent.subframes.add(this);
        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    public InternalDialog(Pane windowPane) {
        super(windowPane);
        this.modality = Modality.APPLICATION_MODAL;

        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    private static InternalFrame findParent(Node node) {

        InternalFrame frame = null;
        Node parent = node;

        while (parent != null && !(parent instanceof InternalFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof InternalFrame) {
            frame = (InternalFrame) parent;
        }

        return frame;
    }

    @Override
    public InternalDialog title(String value) {
        super.title(value);
        return this;
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

    void disableOthers() {

    }

    public void show(Node node) {
        applyModality();
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
        deactivateFront();
        windowPane.getChildren().add(this);
        activateAll();
    }

    public void close() {
        super.close();
        windowPane.getChildren().remove(this);
    }

    void activateAll() {

        var root = getRoot();
        if (root != null) {
            root.activateRoot();
        }
        var modalFrame = getModalFrame(this);

        if (modalFrame == null || modalFrame == this) {
            if (root != null) {
                root.deactivate();
            }
            deactivateFront();
            active.set(true);
            toFront();
            focusOwner.requestFocus();
        } else {
            modalFrame.doModalEffect();
        }
    }

    void activate() {
        active.set(true);
        toFront();
        focusOwner.requestFocus();
    }
}

package dev.jfxde.jfx.scene.control;

import dev.jfxde.jfx.scene.layout.LayoutUtils;
import javafx.beans.value.ChangeListener;
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
        this(findPaneParent(node), modality);
    }

    private InternalDialog(PaneParent paneParent, Modality modality) {
        super(paneParent.getPane());
        this.parent = paneParent.getParent();
        this.modality = modality;

        if (this.parent != null) {
            this.parent.subframes.add(this);
        } else {
            this.modality = Modality.APPLICATION_MODAL;
        }

        addButtons();
        buildLayout(windowPane.getWidth() / 2, windowPane.getHeight() / 2);
        setMoveable();
        setHandlers();
    }

    private static PaneParent findPaneParent(Node node) {

        PaneParent paneParent = null;
        Node parent = node;

        while (parent != null && !(parent instanceof InternalFrame)) {
            parent = parent.getParent();
        }

        if (parent instanceof InternalFrame) {
            paneParent = new PaneParent((InternalFrame) parent);
        } else if (node instanceof Pane) {
            paneParent = new PaneParent((Pane) node);
        }

        return paneParent;
    }

    @Override
    public InternalDialog setTitle(String value) {
        super.setTitle(value);
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

    private ChangeListener<Number> prefSizeListener;

    public void show() {
        applyModality();

        if (!isUseComputedSize()) {
            payload.setPrefWidth(windowPane.getWidth() / 2);
            payload.setPrefHeight(USE_COMPUTED_SIZE);
        }

        prefSizeListener = (v, o, n) -> {
            if (payload.getPrefHeight() == USE_COMPUTED_SIZE) {
                if (payload.getPrefWidth() != USE_COMPUTED_SIZE) {
                    payload.setPrefHeight(Math.min(n.doubleValue(), windowPane.getHeight() - 20));
                }
                center();
                payload.heightProperty().removeListener(prefSizeListener);
                prefSizeListener = null;
            }
        };

        payload.heightProperty().addListener(prefSizeListener);

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
            setActive(true);
            toFront();
            focusOwner.requestFocus();
        } else {
            modalFrame.doModalEffect();
        }
    }

    public void activate() {
        setActive(true);
        toFront();
        focusOwner.requestFocus();
    }

    private static class PaneParent {
        private Pane pane;
        private InternalFrame parent;

        public PaneParent(Pane pane) {
            this.pane = pane;
        }

        public PaneParent(InternalFrame parent) {
            this.parent = parent;
            this.pane = parent.windowPane;
        }

        public Pane getPane() {
            return pane;
        }

        public InternalFrame getParent() {
            return parent;
        }
    }
}

package dev.jfxde.jfx.scene.control;

import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;

public class InternalDialog extends InternalFrame {

    public InternalDialog(Node node) {
        this(node, Modality.NONE);
    }

    public InternalDialog(Node node, Modality modality) {
        super(node, modality);
    }

    @Override
    public InternalDialog setTitle(String value) {
        super.setTitle(value);
        return this;
    }

    @Override
    protected void setHandlers() {
        super.setHandlers();
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
    }

    private ChangeListener<Number> prefSizeListener;

    public void show() {

        if (getPane().getChildren().contains(this)) {
            deactivateFront();
            activateAll();
            return;
        }

        applyModality();

        if (!isUseComputedSize()) {
            getPayload().setPrefWidth(getPane().getWidth() / 2);
            getPayload().setPrefHeight(USE_COMPUTED_SIZE);
        }

        prefSizeListener = (v, o, n) -> {
            if (getPayload().getPrefHeight() == USE_COMPUTED_SIZE) {
                if (getPayload().getPrefWidth() != USE_COMPUTED_SIZE) {
                    getPayload().setPrefHeight(Math.min(n.doubleValue(), getPane().getHeight() - 20));
                }
                center();
                getPayload().heightProperty().removeListener(prefSizeListener);
                prefSizeListener = null;
            }
        };

        getPayload().heightProperty().addListener(prefSizeListener);

        deactivateFront();
        getPane().getChildren().add(this);
        activateAll();
    }

    public void close() {
        super.close();
        getPane().getChildren().remove(this);
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
            requestFocus();
        } else {
            modalFrame.doModalEffect();
        }
    }

    public void activate() {
        setActive(true);
        toFront();
        requestFocus();
    }
}

package dev.jfxde.jfxext.control;

import java.util.HashMap;
import java.util.Map;

import dev.jfxde.jfxext.animation.DropShadowTransition;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.CacheHint;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.stage.Modality;

public abstract class InternalFrame extends Region {

    protected static final double CURSOR_BORDER_WIDTH = 5;
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    protected Modality modality = Modality.NONE;

    protected ObservableList<InternalFrame> subframes = FXCollections.observableArrayList();
    protected Pane windowPane;
    protected InternalFrame parent;
    protected Label titleLabel = new Label();
    protected HBox buttonBox = new HBox();
    protected BorderPane titleBar = new BorderPane();
    protected BorderPane payload = new BorderPane();
    protected ContentRegion contentRegion = new ContentRegion();
    protected Node focusOwner = contentRegion;

    protected Button close = new Button("x");

    protected Bounds restoreBounds;
    protected Point2D pressDragPoint;

    private Map<StringProperty, String> stringProperties = new HashMap<>();

    protected BooleanProperty active = new BooleanPropertyBase() {
        @Override
        protected void invalidated() {
            pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, get());
        }

        @Override
        public String getName() {
            return "activePseudoClass";
        }

        @Override
        public Object getBean() {
            return InternalFrame.this;
        }
    };

    public InternalFrame(Pane windowPane) {
        this.windowPane = windowPane;
        setEffect();
    }

    protected void addButtons() {
        close.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        close.getTooltip().setText("Close");
        stringProperties.put(close.getTooltip().textProperty(), "close");
    }

    protected void buildLayout(double width, double height) {
        buttonBox.setMinWidth(USE_PREF_SIZE);
        buttonBox.setMinHeight(USE_PREF_SIZE);

        titleLabel.setMaxWidth(Double.MAX_VALUE);

        titleBar.setCenter(titleLabel);
        titleBar.setRight(buttonBox);
        titleBar.minWidthProperty().bind(buttonBox.widthProperty().add(10));

        payload.setTop(titleBar);
        payload.setCenter(contentRegion);

        titleBar.getStyleClass().add("jd-frame-title-bar");
        contentRegion.getStyleClass().add("jd-frame-content");

        payload.getStyleClass().add("jd-frame-payload");
        getStyleClass().add("jd-frame");
        payload.minWidthProperty().bind(titleBar.minWidthProperty().add(10));
        payload.minHeightProperty().bind(titleBar.heightProperty().add(10));

        payload.setPrefSize(width, height);
        getChildren().add(payload);
        center();
        restoreBounds = getBoundsInParent();
    }

    private void setEffect() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(10.0);
        shadow.setOffsetX(0);
        shadow.setOffsetY(0);
        shadow.setWidth(20);
        shadow.setHeight(20);
        shadow.setSpread(0.5);
        shadow.setColor(Color.color(0.4, 0.5, 0.5));
        setEffect(shadow);
        setCache(true);
        setCacheHint(CacheHint.SPEED);
    }

    boolean isUseComputedSize() {
        return payload.getPrefWidth() == Region.USE_COMPUTED_SIZE || payload.getPrefHeight() == Region.USE_COMPUTED_SIZE;
    }

    void setUseComputedSize() {

        payload.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
    }

    public InternalFrame setTitle(String value) {
        titleLabel.setText(value);

        return this;
    }

    public InternalFrame setContent(Node node) {
        contentRegion.setContent(node);
        Object owner = node.getProperties().get(node.getClass());
        focusOwner = owner != null ? (Node) owner : node;
        return this;
    }

    public void show() {
    }

    protected void removeContent() {
        contentRegion.removeContent();
    }

    InternalFrame getRoot() {
        InternalFrame root = parent;

        if (root == null) {
            return root;
        }

        while (root.parent != null) {
            root = root.parent;
        }

        return root;
    }

    public abstract void activate();

    protected void activateRoot() {

    }

    void setFreeze(boolean value) {
        if (value) {
            if (!payload.isDisabled()) {
                payload.setDisable(value);
                subframes.stream().filter(s -> s.modality == Modality.NONE).forEach(f -> f.setFreeze(value));
            }
        } else {
            if (payload.isDisabled()) {
                payload.setDisable(value);
                subframes.stream().forEach(f -> f.setFreeze(value));
            }
        }
    }

    boolean isFrozen() {
        return payload.isDisabled();
    }

    void deactivateFront() {

        if (windowPane.getChildren().isEmpty()) {
            return;
        }

        var frame = windowPane.getChildren().get(windowPane.getChildren().size() - 1);

        if (frame instanceof InternalFrame) {
            ((InternalFrame) frame).deactivate();
        }
    }

    void deactivate() {
        setCursor(Cursor.DEFAULT);
        if (isActive()) {
            active.set(false);
            focusOwner = getScene().getFocusOwner();
        }
    }

    public void deactivateAll() {
        deactivate();

        subframes.forEach(InternalFrame::deactivateAll);
    }

    protected void setSubFramesVisible(boolean value) {
        subframes.forEach(s -> s.setSubFramesVisible(value));
    }

    protected void close() {
        subframes.forEach(InternalFrame::close);

        if (parent != null) {
            parent.subframes.remove(this);
            parent.setFreeze(false);
            parent.activate();
        }

        if (modality == Modality.APPLICATION_MODAL) {
            windowPane.toBack();
        }
    }

    boolean isActive() {
        return active.get();
    }

    @Override
    public void toFront() {
        super.toFront();
        subframes.forEach(InternalFrame::toFront);
    }

    boolean isInFront() {
        return !windowPane.getChildren().isEmpty() && windowPane.getChildren().indexOf(this) == windowPane.getChildren().size() - 1;
    }

    void center() {
        if (isUseComputedSize()) {
            relocate((windowPane.getWidth() - payload.getWidth()) / 2, (windowPane.getHeight() - payload.getHeight()) / 2);
        } else {
            relocate((windowPane.getWidth() - payload.getPrefWidth()) / 2, (windowPane.getHeight() - payload.getPrefHeight()) / 2);
        }
    }

    void applyModality() {
        if (modality == Modality.WINDOW_MODAL) {
            if (parent != null) {
                parent.setFreeze(true);
            }
        } else if (modality == Modality.APPLICATION_MODAL) {
            windowPane.toFront();
        }
    }

    protected InternalFrame getModalFrame(InternalFrame frame) {
        InternalFrame modalFrame = null;
        if (frame.isFrozen()) {
            var tmp = subframes.stream().filter(s -> s.modality != Modality.NONE).findFirst().orElse(null);
            while (tmp != null) {
                modalFrame = tmp;
                tmp = tmp.subframes.stream().filter(s -> s.modality != Modality.NONE).findFirst().orElse(null);
            }
        }

        return modalFrame;
    }

    boolean isAncestor(InternalFrame frame) {
        var ancestor = parent;
        while (ancestor != frame && ancestor != null) {
            ancestor = ancestor.parent;
        }

        return ancestor == frame;
    }

    public void doModalEffect() {

        new DropShadowTransition((DropShadow) getEffect(), this).play();
    }
}

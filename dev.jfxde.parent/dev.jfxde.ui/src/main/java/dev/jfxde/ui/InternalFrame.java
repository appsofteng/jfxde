package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

public abstract class InternalFrame extends Region {

    protected static final double CURSOR_BORDER_WIDTH = 5;
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    protected ObservableList<InternalDialog> subdialogs = FXCollections.observableArrayList();
    protected WindowPane windowPane;
    protected InternalFrame parent;
    protected Label title = new Label();
    protected HBox buttonBox = new HBox();
    protected BorderPane titleBar = new BorderPane();
    protected BorderPane payload = new BorderPane();
    protected ContentRegion contentRegion = new ContentRegion();
    protected Node focusOwner = contentRegion;

    protected Button close = new Button("x");

    protected Bounds restoreBounds;
    protected Point2D pressDragPoint;

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

    public InternalFrame(WindowPane windowPane) {
        this.windowPane = windowPane;
    }

    protected void addButtons() {
        close.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        close.getTooltip().textProperty().bind(Sys.rm().getStringBinding("close"));
    }

    protected void buildLayout(double width, double height) {
        title.setPrefWidth(Double.MAX_VALUE);

        buttonBox.setMinWidth(USE_PREF_SIZE);
        buttonBox.setMinHeight(USE_PREF_SIZE);

        titleBar.setLeft(title);
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

    WindowPane getWindowPane() {
        return windowPane;
    }

    void setContent(Node node) {
        contentRegion.setContent(node);
        focusOwner = node;
    }

    void removeContent() {
        contentRegion.removeContent();
    }

    abstract void activate();

    void freez() {
        payload.setDisable(true);
        subdialogs.stream().forEach(InternalDialog::freez);
    }

    void unfreez() {
        payload.setDisable(false);
        subdialogs.stream().forEach(InternalDialog::unfreez);
    }

    void deactivate() {
        setCursor(Cursor.DEFAULT);
        if (isActive()) {
            active.set(false);
            focusOwner = getScene().getFocusOwner();
        }
    }

    boolean isActive() {
        return active.get();
    }

    @Override
    public void toFront() {
        super.toFront();
        subdialogs.forEach(InternalFrame::toFront);
    }

    boolean isInFront() {
        return !windowPane.getChildren().isEmpty() && windowPane.getChildren().indexOf(this) == windowPane.getChildren().size() - 1;
    }

    void center() {
        relocate((windowPane.getWidth() - payload.getPrefWidth()) / 2, (windowPane.getHeight() - payload.getPrefHeight()) / 2);
    }
}

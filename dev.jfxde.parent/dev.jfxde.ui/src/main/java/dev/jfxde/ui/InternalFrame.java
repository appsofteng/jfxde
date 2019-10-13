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
import javafx.scene.layout.StackPane;

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
    protected StackPane contentPane = new StackPane();
    protected Node focusOwner = contentPane;

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
        close.getStyleClass().addAll("jd-internal-window-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        close.getTooltip().textProperty().bind(Sys.rm().getStringBinding("close"));
    }

    protected void buildLayout(double width, double height) {
        title.setPrefWidth(Double.MAX_VALUE);

        titleBar.setLeft(title);
        titleBar.setRight(buttonBox);
        titleBar.minWidthProperty().bind(buttonBox.widthProperty().add(10));

        payload.setTop(titleBar);
        payload.setCenter(contentPane);

        titleBar.getStyleClass().add("jd-internal-window-title-bar");
        contentPane.getStyleClass().add("jd-internal-window-content");
        payload.getStyleClass().add("jd-internal-window-payload");
        getStyleClass().add("jd-internal-window");
        payload.minWidthProperty().bind(titleBar.minWidthProperty().add(10));
        payload.setMinHeight(70);

        payload.setPrefSize(width, height);
        getChildren().add(payload);
        relocate(width / 2, height / 2);
        restoreBounds = getBoundsInParent();
    }

    WindowPane getWindowPane() {
        return windowPane;
    }

    void setContent(Node node) {
        contentPane.getChildren().add(node);
        focusOwner = node;
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
}

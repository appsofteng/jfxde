package dev.jfxde.jfx.scene.control;

import java.util.List;
import java.util.function.Supplier;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.scene.layout.LayoutUtils;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.BooleanPropertyBase;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.css.PseudoClass;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.BorderWidths;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Modality;
import javafx.util.Duration;

public abstract class InternalFrame extends Region {

    private static final double CURSOR_BORDER_WIDTH = 5;
    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");

    private Pane pane;
    private InternalFrame parent;
    private Modality modality = Modality.NONE;
    private BorderPane payload = new BorderPane();
    private ObservableList<InternalFrame> subframes = FXCollections.observableArrayList();
    private HBox buttonBox = new HBox();
    private BooleanProperty closable = new SimpleBooleanProperty(true);
    private Label titleLabel = new Label();
    private BorderPane titleBar = new BorderPane();
    private Supplier<Node> iconSupplier = () -> null;

    private ContentRegion contentRegion = new ContentRegion();
    private Node focusOwner;

    private Button maximize;
    private Button close;

    private Bounds restoreBounds;
    private Point2D pressDragPoint;

    private Timeline modalTimeline;

    private BooleanProperty active;
    private BooleanProperty maximized = new SimpleBooleanProperty();

    public InternalFrame(Pane pane) {
        this.pane = pane;

        init();
    }

    public InternalFrame(Node node, Modality modality) {
        this(findPaneParent(node), modality);
    }

    private InternalFrame(PaneParent paneParent, Modality modality) {
        this.pane = paneParent.getPane();
        this.parent = paneParent.getParent();
        this.modality = modality;

        if (this.parent != null) {
            this.parent.getSubframes().add(this);
            setIconSupplier(this.parent.iconSupplier);
            setIcon(this.parent.iconSupplier.get());
        } else if (modality == Modality.WINDOW_MODAL) {
            this.modality = Modality.APPLICATION_MODAL;
        }

        init();
    }

    private void init() {
        buildLayout(pane.getWidth() / 2, pane.getHeight() / 2);
        addButtons();
        setHandlers();
        setMoveable();
        setBorder();
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

    protected void addButtons() {

        maximize = new Button(Fonts.Unicode.WHITE_LARGE_SQUARE);
        maximize.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        maximize.setFocusTraversable(false);
        maximize.textProperty()
                .bind(Bindings.when(maximized)
                        .then(Fonts.Unicode.UPPER_RIGHT_DROP_SHADOWED_WHITE_SQUARE)
                        .otherwise(Fonts.Unicode.WHITE_LARGE_SQUARE));
        maximize.setTooltip(new Tooltip());
        maximize.getTooltip().textProperty().bind(Bindings.when(maximized)
                .then(FXResourceBundle.getBundle().getStringBinding("restore"))
                .otherwise(FXResourceBundle.getBundle().getStringBinding("maximize")));
        maximize.setOnAction(e -> onMaximizeRestore());

        close = new Button("x");
        close.getStyleClass().addAll("jd-frame-button", "jd-font-awesome-solid");
        close.setFocusTraversable(false);
        close.setTooltip(new Tooltip());
        FXResourceBundle.getBundle().put(close.getTooltip().textProperty(), "close");
        close.setOnAction(e -> onClose());
        close.disableProperty().bind(closable.not());

        buttonBox.getChildren().addAll(maximize, close);
    }

    protected void addButtons(int index, List<Button> buttons) {
        if (index >= 0) {
            buttonBox.getChildren().addAll(index, buttons);
        } else {
            buttonBox.getChildren().addAll(buttonBox.getChildren().size() + index, buttons);
        }
    }

    private void buildLayout(double width, double height) {
        buttonBox.setMinWidth(USE_PREF_SIZE);
        buttonBox.setMinHeight(USE_PREF_SIZE);

        titleLabel.setMaxWidth(Double.MAX_VALUE);

        titleBar.setCenter(titleLabel);
        titleBar.setRight(buttonBox);
        titleBar.minWidthProperty().bind(buttonBox.widthProperty().add(10));
        titleBar.managedProperty().bind(titleBar.visibleProperty());

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

    protected void setHandlers() {

        sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                getScene().focusOwnerProperty().addListener((vv, oo, nn) -> {

                    if (nn != null) {
                        var prnt = nn.getParent();

                        while (prnt != null && prnt != this) {
                            prnt = prnt.getParent();
                        }

                        if (prnt != null) {
                            focusOwner = nn;
                        }
                    }
                });
            }
        });

        titleLabel.setOnMouseClicked(e -> {
            InternalFrame modalFrame = getModalFrame(this);
            if (modalFrame == null) {
                if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                    onMaximizeRestore();
                }
            } else {
                e.consume();
            }
        });
    }

    private void setMoveable() {
        LayoutUtils.makeDragable(this, titleBar, e -> {
            if (isEnlarged()) {
                Point2D localClickPoint = pane.screenToLocal(e.getScreenX(), e.getScreenY());
                double restoreX = localClickPoint.getX() - restoreBounds.getWidth() / 2;
                restoreX = Math.max(0, restoreX);
                restoreX = Math.min(pane.getWidth() - restoreBounds.getWidth(), restoreX);
                double restoreY = localClickPoint.getY() - e.getY();
                pressDragPoint = new Point2D(restoreX, restoreY);
            } else {
                pressDragPoint = new Point2D(getLayoutX(), getLayoutY());
            }

            return pressDragPoint;
        }, () -> {

            if (isEnlarged()) {
                restoreBounds = new BoundingBox(pressDragPoint.getX(), 0, restoreBounds.getWidth(),
                        restoreBounds.getHeight());
                onRestore();
            }
        });

        LayoutUtils.makeResizable(this, payload, CURSOR_BORDER_WIDTH);
    }

    private void setBorder() {
        Stop[] stops = new Stop[] { new Stop(0, Color.rgb(0, 0, 0, 0.6)), new Stop(0.2, Color.rgb(0, 0, 0, 0.5)),
                new Stop(0.4, Color.rgb(0, 0, 0, 0.4)), new Stop(0.6, Color.rgb(0, 0, 0, 0.3)), new Stop(0.8, Color.rgb(0, 0, 0, 0.2)),
                new Stop(1, Color.rgb(0, 0, 0, 0.1)) };
        RadialGradient rg = new RadialGradient(0, 0, 0.5, 0.5, 1, true, CycleMethod.REFLECT, stops);
        var border = new Border(new BorderStroke(rg, BorderStrokeStyle.SOLID, new CornerRadii(8), new BorderWidths(3), new Insets(-3)));
        setBorder(border);
    }
// More attractive than border but blocks transparency and uses more graphic resources.
//    private void setEffect() {
//        DropShadow shadow = new DropShadow();
//        shadow.setRadius(10.0);
//        shadow.setOffsetX(0);
//        shadow.setOffsetY(0);
//        shadow.setWidth(20);
//        shadow.setHeight(20);
//        shadow.setSpread(0.5);
//        shadow.setColor(Color.color(0.4, 0.5, 0.5));
//        setEffect(shadow);
//        setCache(true);
//        setCacheHint(CacheHint.SPEED);
//    }

    protected ObservableList<InternalFrame> getSubframes() {
        return subframes;
    }

    protected Region getPayload() {
        return payload;
    }

    Pane getPane() {
        return pane;
    }

    boolean isUseComputedSize() {
        return payload.getPrefWidth() == Region.USE_COMPUTED_SIZE || payload.getPrefHeight() == Region.USE_COMPUTED_SIZE;
    }

    protected void setUseComputedSize() {

        payload.setPrefSize(USE_COMPUTED_SIZE, USE_COMPUTED_SIZE);
        maximize.setVisible(false);
    }

    protected BooleanProperty titleVisibleProperty() {
        return titleBar.visibleProperty();
    }

    protected void setIconSupplier(Supplier<Node> supplier) {
        this.iconSupplier = supplier;
    }

    protected void setIcon(Node node) {
        titleLabel.setGraphic(node);
    }

    public InternalFrame setTitle(String value) {
        titleLabel.setText(value);

        return this;
    }

    protected StringProperty titleProperty() {
        return titleLabel.textProperty();
    }

    protected void setTitleContextMenu(ContextMenu menu) {
        titleLabel.setContextMenu(menu);
    }

    public InternalFrame setContent(Node node) {
        contentRegion.setContent(node);

        return this;
    }

    protected void setFocusOwner(Node node) {
        this.focusOwner = node;
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

    public void setContentCss(String css) {
        if (css != null) {
            contentRegion.getStylesheets().addAll(css);
        }
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

        if (pane.getChildren().isEmpty()) {
            return;
        }

        var frame = pane.getChildren().get(pane.getChildren().size() - 1);

        if (frame instanceof InternalFrame) {
            ((InternalFrame) frame).deactivate();
        }
    }

    void deactivate() {
        setCursor(Cursor.DEFAULT);
        if (isActive()) {
            setActive(false);
        }
    }

    public void deactivateAll() {
        deactivate();

        subframes.forEach(InternalFrame::deactivateAll);
    }

    protected void setSubFramesVisible(boolean value) {
        subframes.forEach(s -> {
            s.setVisible(value);
            s.setSubFramesVisible(value);
        });
    }

    @Override
    public boolean isResizable() {
        return !isMaximized();
    }

    protected boolean isEnlarged() {
        return isMaximized();
    }

    protected void onRestore() {
        restore();
    }

    private boolean isMaximized() {
        return maximized.get();
    }

    protected void setMaximized(boolean value) {
        this.maximized.set(value);
    }

    protected boolean isRestored() {
        return !isMaximized();
    }

    protected void onMaximizeRestore() {

        if (isMaximized()) {
            restore();
        } else {
            storeBounds();
            maximize();
        }
    }

    protected void storeBounds() {
        restoreBounds = getBoundsInParent();
    }

    protected void maximize() {

        if (isUseComputedSize()) {
            return;
        }

        layoutXProperty().unbind();
        layoutYProperty().unbind();
        setLayoutX(0);
        setLayoutY(0);

        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();
        payload.prefWidthProperty().bind(pane.widthProperty());
        payload.prefHeightProperty().bind(pane.heightProperty());

        toFront();

        setMaximized(true);
    }

    protected void restore() {
        layoutXProperty().unbind();
        layoutYProperty().unbind();
        setLayoutX(restoreBounds.getMinX());
        setLayoutY(restoreBounds.getMinY());

        payload.prefWidthProperty().unbind();
        payload.prefHeightProperty().unbind();
        payload.setPrefSize(restoreBounds.getWidth(), restoreBounds.getHeight());

        setMaximized(false);
    }

    public boolean isClosable() {
        return closable.get();
    }

    public BooleanProperty closableProperty() {
        return closable;
    }

    protected void onClose() {
        close();
    }

    protected void close() {
        subframes.forEach(f -> {
            f.parent = null;
            f.close();
        });

        if (parent != null) {
            parent.subframes.remove(this);
            parent.setFreeze(false);
            parent.activate();
        }

        if (modality == Modality.APPLICATION_MODAL) {
            pane.toBack();
        }
    }

    boolean isActive() {
        return active == null ? false : activeProperty().get();
    }

    protected void setActive(boolean value) {
        activeProperty().set(value);
    }

    BooleanProperty activeProperty() {
        if (active == null) {
            active = new BooleanPropertyBase() {
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
        }

        return active;
    }

    @Override
    public void requestFocus() {
        Platform.runLater(() -> {
            if (focusOwner != null) {
                focusOwner.requestFocus();
            }
        });
    }

    @Override
    public void toFront() {
        super.toFront();
        subframes.forEach(InternalFrame::toFront);
    }

    boolean isInFront() {
        return !pane.getChildren().isEmpty() && pane.getChildren().indexOf(this) == pane.getChildren().size() - 1;
    }

    void center() {
        if (isUseComputedSize()) {
            relocate((pane.getWidth() - payload.getWidth()) / 2, (pane.getHeight() - payload.getHeight()) / 2);
        } else {
            relocate((pane.getWidth() - payload.getPrefWidth()) / 2, (pane.getHeight() - payload.getPrefHeight()) / 2);
        }
    }

    void applyModality() {
        if (modality == Modality.WINDOW_MODAL) {
            if (parent != null) {
                parent.setFreeze(true);
            }
        } else if (modality == Modality.APPLICATION_MODAL) {
            pane.toFront();
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

        if (modalTimeline != null) {
            modalTimeline.stop();
        } else {

            var b1 = new Border(new BorderStroke(Color.rgb(255, 255, 255, 0.5), BorderStrokeStyle.SOLID, new CornerRadii(5), new BorderWidths(4),
                    new Insets(-4)));
            var b2 = getBorder();
            modalTimeline = new Timeline();
            modalTimeline.setCycleCount(5);
            modalTimeline.setRate(15);
            modalTimeline.setOnFinished(e -> setBorder(b2));

            modalTimeline.getKeyFrames().setAll(
                    new KeyFrame(Duration.seconds(1),
                            new KeyValue(borderProperty(), b1)),
                    new KeyFrame(Duration.seconds(2),
                            new KeyValue(borderProperty(), b2)));
        }

        modalTimeline.play();

//        new DropShadowTransition((DropShadow) getEffect(), this).play();
    }

    private static class PaneParent {
        private Pane pane;
        private InternalFrame parent;

        public PaneParent(Pane pane) {
            this.pane = pane;
        }

        public PaneParent(InternalFrame parent) {
            this.parent = parent;
            this.pane = parent.pane;
        }

        public Pane getPane() {
            return pane;
        }

        public InternalFrame getParent() {
            return parent;
        }
    }
}

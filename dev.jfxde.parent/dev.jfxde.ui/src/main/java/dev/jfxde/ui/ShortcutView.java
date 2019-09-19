package dev.jfxde.ui;

import dev.jfxde.logic.ResourceManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.Shortcut;
import javafx.animation.PauseTransition;
import javafx.beans.binding.Bindings;
import javafx.css.PseudoClass;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextFormatter;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Screen;
import javafx.util.Duration;

public class ShortcutView extends VBox {

    private static final PseudoClass ACTIVE_PSEUDO_CLASS = PseudoClass.getPseudoClass("active");
    private static double SIZE = 70;
    private static int MAX_CHARS = 100;
    private Shortcut shortcut;
    private final TextArea name;
    private Text helperHeightText;
    private final double prefTextHeight;
    private PauseTransition pauseBeforeEdit;
    private Point2D namePressedPoint;

    public ShortcutView(Shortcut shortcut) {
        this.shortcut = shortcut;

        AppProviderDescriptor appProviderDescriptor = Sys.am().getAppProviderDescriptor(shortcut.getFqn());
        Region icon = null;
        if (appProviderDescriptor != null) {
            icon = appProviderDescriptor.getMediumIcon("jd-desktop-shortcut-icon");
        } else {
            Label iconLabel = new Label("?");
            iconLabel.setContentDisplay(ContentDisplay.TEXT_ONLY);
            iconLabel.getStyleClass().add("jd-desktop-shortcut-icon");
            iconLabel.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
            iconLabel.setPrefSize(ResourceManager.MEDIUM_ICON_SIZE, ResourceManager.MEDIUM_ICON_SIZE);
            iconLabel.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
            icon = iconLabel;
        }

        name = new TextArea(shortcut.getName());
        name.getStyleClass().add("jd-desktop-shortcut-name");
        name.setWrapText(true);
        name.setTextFormatter(new TextFormatter<>(change -> change.getControlNewText().length() <= MAX_CHARS ? change : null));

        helperHeightText = new Text("Yy\nYy");
        helperHeightText.setFont(name.getFont());
        helperHeightText.setWrappingWidth(SIZE - 1);
        prefTextHeight = helperHeightText.getBoundsInParent().getHeight() + 4;
        helperHeightText.textProperty().bind(name.textProperty());
        helperHeightText.setLineSpacing(1);

        name.setPrefHeight(prefTextHeight);

        stopEditing();

        name.editableProperty().addListener((v, o, n) -> {
            if (!n && !shortcut.getName().equals(name.getText())) {
                shortcut.setName(name.getText());
                Sys.dm().updateShortcut(shortcut);
            }
        });

        getStyleClass().add("jd-desktop-shortcut-view");
        getChildren().addAll(icon, name);

        setPrefSize(SIZE, SIZE);
        setMinSize(SIZE, SIZE);
        setMaxSize(SIZE, USE_PREF_SIZE);
        setAlignment(Pos.CENTER);

        setHandlers();
        setContextMenu();
    }

    private void setHandlers() {

        setOnMousePressed(e -> {
            e.consume();

            if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2) {
                if (pauseBeforeEdit != null) {
                    pauseBeforeEdit.stop();
                    pauseBeforeEdit = null;
                }

                namePressedPoint = null;
                Sys.am().start(shortcut.getFqn(), shortcut.getUri());
                return;
            }

            if (e.getButton() == MouseButton.PRIMARY) {
                if (shortcut.isActive()) {
                    if (name.isEditable()) {
                        stopEditing();
                    }
                }
            }

            if (!shortcut.isActive()) {
                shortcut.activate();
            } else {
                namePressedPoint = name.parentToLocal(e.getX(), e.getY());
            }

        });

        setOnMouseClicked(e -> {

            if (e.getButton() == MouseButton.PRIMARY && namePressedPoint != null && name.contains(namePressedPoint) && !name.isEditable()) {

                pauseBeforeEdit = new PauseTransition(Duration.seconds(1));
                pauseBeforeEdit.setOnFinished(w -> startEditing());
                pauseBeforeEdit.play();

            }
            e.consume();
        });

        setOnKeyPressed(e -> {
            if (name.isEditable() && e.getCode() == KeyCode.ESCAPE) {
                name.undo();
                stopEditing();
            }
        });

        shortcut.activeProperty().addListener((v, o, n) -> {
            if (n) {
                activate();
            } else {
                deactivate();
            }

        });
    }

    private void setContextMenu() {
        MenuItem delete = new MenuItem();
        delete.textProperty().bind(Sys.rm().getTextBinding("delete"));
        delete.setOnAction(e -> Sys.dm().removeShortcut(shortcut));
        ContextMenu contextMenu = new ContextMenu();
        contextMenu.getItems().add(delete);
        contextMenu.setAutoHide(true);

        setOnContextMenuRequested(e -> {
            contextMenu.show(getScene().getWindow(), e.getScreenX(), e.getScreenY());
            e.consume();
        });

    }

    public Shortcut getShortcut() {
        return shortcut;
    }

    public Point2D getCoordinatesForPosition() {

        int position = shortcut.getPosition();

        int totalRows = getTotalRows();
        int rows = position % totalRows;
        int cols = position / totalRows;

        double x = cols * SIZE;
        double y = rows * SIZE;

        Point2D xy = new Point2D(x, y);

        return xy;
    }

    public void moved(double x, double y) {
        setPressed(false);
        int position = getPositionForCoordinates(x, y);
        shortcut.moved(position);
    }

    private int getPositionForCoordinates(double x, double y) {
        int totalRows = getTotalRows();
        int rows = (int) (y / SIZE);
        int cols = (int) (x / SIZE);
        int position = totalRows * cols + rows;

        return position;
    }

    private int getTotalRows() {
        return (int) (Screen.getPrimary().getVisualBounds().getHeight() / SIZE);
    }

    private void startEditing() {
        name.setEditable(true);
        name.setDisable(false);
        name.requestFocus();
    }

    public void stopEditing() {
        if (name.getText().isEmpty()) {
            name.undo();
        }
        name.deselect();
        name.positionCaret(0);
        name.setScrollTop(0);
        name.setEditable(false);
        name.setDisable(true);
        requestFocus();
    }

    public Image getSnapshot() {
        SnapshotParameters parameters = new SnapshotParameters();
        parameters.setFill(Color.TRANSPARENT);
        Image snapshot = snapshot(parameters, null);

        return snapshot;
    }

    private void activate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, true);
        name.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> Math.max(prefTextHeight, helperHeightText.getLayoutBounds().getHeight()),
                helperHeightText.textProperty()));

        setPrefHeight(USE_COMPUTED_SIZE);
        requestFocus();
    }

    private void deactivate() {
        pseudoClassStateChanged(ACTIVE_PSEUDO_CLASS, false);
        name.prefHeightProperty().unbind();
        name.setPrefHeight(prefTextHeight);

        setPrefHeight(SIZE);
        stopEditing();
        namePressedPoint = null;
    }

}

package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.Desktop;
import dev.jfxde.logic.data.Shortcut;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.HPos;
import javafx.geometry.Point2D;
import javafx.geometry.VPos;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;

public class ShortcutPane extends Pane {

    private Desktop desktop;

    public ShortcutPane(Desktop desktop) {
        this.desktop = desktop;
        getStyleClass().add("jd-desktop-shortcut-pane");

        setListeners();
        desktop.getShortcuts().forEach(s -> addShortcutView(s));
    }

    @Override
    protected void layoutChildren() {

        getChildren().forEach(c -> {
            ShortcutView sw = (ShortcutView) c;
            Point2D xy = sw.getCoordinatesForPosition();

            sw.autosize();
            layoutInArea(c, xy.getX(), xy.getY(), sw.getWidth(), sw.getHeight(), 0, HPos.LEFT, VPos.CENTER);
        });
    }

    private void setListeners() {

        setOnMouseClicked(e -> {
            desktop.setActiveShortcut(null);
            desktop.setActiveWindow(null);
            requestFocus();
        });

        desktop.getShortcuts().addListener((Change<? extends Shortcut> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(sc -> addShortcutView(sc));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(s -> removeShortcutView(s));
                }
            }
        });

        setOnDragOver(event -> {
            if (event.getDragboard().hasString() && event.getGestureSource() instanceof ShortcutView) {

                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }

            event.consume();
        });

        setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasString() && event.getGestureSource() instanceof ShortcutView) {
                ShortcutView shortcutViewSource = (ShortcutView) event.getGestureSource();
                shortcutViewSource.moved(event.getX(), event.getY());
                success = true;
                layoutChildren();
                Sys.dm().updateShortcut(shortcutViewSource.getShortcut());
            }

            event.setDropCompleted(success);

            event.consume();
        });
    }

    private void addShortcutView(Shortcut shortcut) {

        ShortcutView shortcutView = new ShortcutView(shortcut);

        shortcutView.setOnDragDetected(event -> {
            shortcutView.stopEditing();
            Dragboard db = shortcutView.startDragAndDrop(TransferMode.MOVE);
            // db.setDragView(new Text(shortcut.getName()).snapshot(null, null),
            // event.getX(), event.getY());
            db.setDragView(shortcutView.getSnapshot(), event.getX(), event.getY());
            // db.setDragView(shortcutView.getName().snapshot(null, null),
            // event.getX(), event.getY());
            ClipboardContent content = new ClipboardContent();
            content.putString(shortcut.getName());
            db.setContent(content);
            event.consume();
        });

        getChildren().add(shortcutView);
    }

    private void removeShortcutView(Shortcut shortcut) {

        getChildren().removeIf(n -> ((ShortcutView) n).getShortcut() == shortcut);
    }

}

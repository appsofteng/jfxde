package dev.jfxde.fxmisc.richtext;

import java.util.HashMap;
import java.util.Map;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class ContextMenuBuilder {

    private final GenericStyledArea<?, ?, ?> area;
    private Map<StringProperty, String> stringProperties = new HashMap<>();

    private ContextMenuBuilder(GenericStyledArea<?, ?, ?> codeArea) {
        this.area = codeArea;
    }

    public static ContextMenuBuilder get(GenericStyledArea<?, ?, ?> codeArea) {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);
        menu.setHideOnEscape(true);
        menu.setConsumeAutoHidingEvents(true);
        menu.addEventHandler(KeyEvent.ANY, e -> e.consume());
        codeArea.setContextMenu(menu);
        return new ContextMenuBuilder(codeArea);
    }

    public ContextMenuBuilder copy() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        item.setText("Copy");
        stringProperties.put(item.textProperty(), "copy");
        item.setOnAction(e -> area.copy());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder cut() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        item.setText("Cut");
        stringProperties.put(item.textProperty(), "cut");
        item.setOnAction(e -> area.cut());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder paste() {

        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        item.setText("Paste");
        stringProperties.put(item.textProperty(), "paste");
        item.setOnAction(e -> area.paste());

        area.getContextMenu().setOnShowing(e -> {
            item.setDisable(!Clipboard.getSystemClipboard().hasString());
        });
        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder selectAll() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        item.setText("Select All");
        stringProperties.put(item.textProperty(), "selectAll");
        item.setOnAction(e -> area.selectAll());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelectedText().length() == area.getText().length(),
                area.selectedTextProperty()));
        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder undo() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+Z"));
        item.setText("Undo");
        stringProperties.put(item.textProperty(), "undo");
        item.setOnAction(e -> area.undo());
        // item.disableProperty().bind(Bindings.createBooleanBinding(() ->
        // !area.getUndoManager().isUndoAvailable(),
        // area.getUndoManager().undoAvailableProperty()));
        item.setDisable(true);
        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> item.setDisable(n == null || !(Boolean) n));
        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder redo() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+Y"));
        item.setText("Redo");
        stringProperties.put(item.textProperty(), "redo");
        item.setOnAction(e -> area.redo());
        // item.disableProperty().bind(Bindings.createBooleanBinding(() ->
        // !area.getUndoManager().isRedoAvailable(),
        // area.getUndoManager().redoAvailableProperty()));
        item.setDisable(true);
        area.getUndoManager().redoAvailableProperty().addListener((v, o, n) -> item.setDisable(n == null || !(Boolean) n));
        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder separator() {

        area.getContextMenu().getItems().add(new SeparatorMenuItem());

        return this;
    }

    public ContextMenuBuilder clear() {
        MenuItem item = new MenuItem();
        item.setText("Clear");
        stringProperties.put(item.textProperty(), "clear");
        item.setOnAction(e -> area.clear());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getLength() == 0, area.lengthProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder clear(EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem();
        item.setText("clear");
        stringProperties.put(item.textProperty(), "clear");
        item.setOnAction(handler);
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getLength() == 0, area.lengthProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }
}

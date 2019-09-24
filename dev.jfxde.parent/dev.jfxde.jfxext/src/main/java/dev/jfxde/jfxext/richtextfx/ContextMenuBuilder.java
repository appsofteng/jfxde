package dev.jfxde.jfxext.richtextfx;

import org.fxmisc.richtext.GenericStyledArea;

import dev.jfxde.jfxext.util.FXResourceBundle;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class ContextMenuBuilder {

    private final GenericStyledArea<?,?,?> area;
    private String baseName;

    private ContextMenuBuilder(GenericStyledArea<?,?,?> codeArea) {
        this.area = codeArea;
        this.baseName = FXResourceBundle.class.getPackageName() + ".strings";
    }

    public static ContextMenuBuilder get(GenericStyledArea<?,?,?> codeArea) {
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
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("copy"));
        item.setOnAction(e -> area.copy());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder cut() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("cut"));
        item.setOnAction(e -> area.cut());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelection().getLength() == 0, area.selectionProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder paste() {

        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("paste"));
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
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("selectAll"));
        item.setOnAction(e -> area.selectAll());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getSelectedText().length() == area.getText().length(),
                area.selectedTextProperty()));
        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder clear() {
        MenuItem item = new MenuItem();
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("clear"));
        item.setOnAction(e -> area.clear());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getLength() == 0, area.lengthProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder clear(EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem();
        item.textProperty().bind(FXResourceBundle.getBundle​(baseName).getStringBinding("clear"));
        item.setOnAction(handler);
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> area.getLength() == 0, area.lengthProperty()));

        area.getContextMenu().getItems().add(item);

        return this;
    }
}

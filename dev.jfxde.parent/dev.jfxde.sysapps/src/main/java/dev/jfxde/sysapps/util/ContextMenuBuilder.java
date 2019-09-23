package dev.jfxde.sysapps.util;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.input.Clipboard;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;

public class ContextMenuBuilder {

    private final CodeArea codeArea;
    private final AppContext context;

    private ContextMenuBuilder(CodeArea codeArea, AppContext context) {
        this.codeArea = codeArea;
        this.context = context;
    }

    public static ContextMenuBuilder get(CodeArea codeArea, AppContext context) {
        ContextMenu menu = new ContextMenu();
        menu.setAutoHide(true);
        menu.setHideOnEscape(true);
        menu.setConsumeAutoHidingEvents(true);
        menu.addEventHandler(KeyEvent.ANY, e -> e.consume());
        codeArea.setContextMenu(menu);
        return new ContextMenuBuilder(codeArea, context);
    }

    public ContextMenuBuilder copy() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+C"));
        item.textProperty().bind(context.rc().getStringBinding("copy"));
        item.setOnAction(e -> codeArea.copy());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getSelection().getLength() == 0, codeArea.selectionProperty()));

        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder cut() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+X"));
        item.textProperty().bind(context.rc().getStringBinding("cut"));
        item.setOnAction(e -> codeArea.cut());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getSelection().getLength() == 0, codeArea.selectionProperty()));

        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder paste() {

        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+V"));
        item.textProperty().bind(context.rc().getStringBinding("paste"));
        item.setOnAction(e -> codeArea.paste());

        codeArea.getContextMenu().setOnShowing(e -> {
            item.setDisable(!Clipboard.getSystemClipboard().hasString());
        });
        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder selectAll() {
        MenuItem item = new MenuItem();
        item.setAccelerator(KeyCombination.keyCombination("Shortcut+A"));
        item.textProperty().bind(context.rc().getStringBinding("selectAll"));
        item.setOnAction(e -> codeArea.selectAll());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getSelectedText().length() == codeArea.getText().length(),
                codeArea.selectedTextProperty()));
        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder clear() {
        MenuItem item = new MenuItem();
        item.textProperty().bind(context.rc().getStringBinding("clear"));
        item.setOnAction(e -> codeArea.clear());
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getLength() == 0, codeArea.lengthProperty()));

        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

    public ContextMenuBuilder clear(EventHandler<ActionEvent> handler) {
        MenuItem item = new MenuItem();
        item.textProperty().bind(context.rc().getStringBinding("clear"));
        item.setOnAction(handler);
        item.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getLength() == 0, codeArea.lengthProperty()));

        codeArea.getContextMenu().getItems().add(item);

        return this;
    }

}

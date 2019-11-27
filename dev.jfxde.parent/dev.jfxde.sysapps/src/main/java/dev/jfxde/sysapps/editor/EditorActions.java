package dev.jfxde.sysapps.editor;

import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.event.Event;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;

public class EditorActions {

    private EditorContent content;
    private Action saveAction;
    private Action saveAllAction;
    private Action findAction;

    public EditorActions(EditorContent content) {
        this.content = content;
    }

    void init() {
        createActions();
        setListeners();
    }

    private void createActions() {
        saveAction = new Action(this::save);
        saveAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_SOLID).create(Fonts.Unicode.FLOPPY_DISK).size(14));
        saveAction.disabledProperty().set(true);
        FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
        FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "save");
        saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));

        saveAllAction = new Action(this::saveAll);
        saveAllAction.setGraphic(new ImageView(getClass().getResource("save-all.png").toExternalForm()));
        FXResourceBundle.getBundle().put(saveAllAction.textProperty(), "saveAll");
        FXResourceBundle.getBundle().put(saveAllAction.longTextProperty(), "saveAll");
        saveAllAction.setAccelerator(KeyCombination.keyCombination("Shift+Shortcut+S"));

        findAction = new Action(this::find);
        FXResourceBundle.getBundle().put(findAction.textProperty(), "find");
        FXResourceBundle.getBundle().put(findAction.longTextProperty(), "find");
        findAction.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));
    }

    private void setListeners() {
        saveAllAction.disabledProperty().bind(content.getEditorPane().changedProperty().not());

        content.getEditorPane().selectedEditorProperty().addListener((v,o,n) -> {
            saveAction.disabledProperty().unbind();
            if (n != null) {
                saveAction.disabledProperty().bind(n.changedProperty().not());
            } else {
                saveAction.disabledProperty().set(true);
            }
        });
    }

    Action saveAction() {
        return saveAction;
    }

    Action saveAllAction() {
        return saveAllAction;
    }

    private void save(Event e) {
        content.getEditorPane().save();
    }

    private void saveAll(Event e) {
        content.getEditorPane().saveAll();
    }

    private void find(Event e) {
        new FindDialog(content).show();
    }
}

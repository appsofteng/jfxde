package dev.jfxde.sysapps.editor;

import org.controlsfx.control.action.Action;
import org.controlsfx.glyphfont.GlyphFontRegistry;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCombination;

public class EditorActions {

    private EditorContent content;
    private Action saveAction;
    private Action saveAllAction;
    private Action findAction;
    private Action goToLineAction;
    private Action showInFavoritesAction;

    public EditorActions(EditorContent content) {
        this.content = content;
    }

    void init() {
        createActions();
        setListeners();
    }

    private void createActions() {
        saveAction = new Action(e -> content.getEditorPane().save());
        saveAction.setGraphic(GlyphFontRegistry.font(Fonts.FONT_AWESOME_5_FREE_SOLID).create(Fonts.Unicode.FLOPPY_DISK).size(14));
        saveAction.disabledProperty().set(true);
        FXResourceBundle.getBundle().put(saveAction.textProperty(), "save");
        FXResourceBundle.getBundle().put(saveAction.longTextProperty(), "save");
        saveAction.setAccelerator(KeyCombination.keyCombination("Shortcut+S"));

        saveAllAction = new Action(e -> content.getEditorPane().saveAll());
        saveAllAction.setGraphic(new ImageView(getClass().getResource("save-all.png").toExternalForm()));
        FXResourceBundle.getBundle().put(saveAllAction.textProperty(), "saveAll");
        FXResourceBundle.getBundle().put(saveAllAction.longTextProperty(), "saveAll");
        saveAllAction.setAccelerator(KeyCombination.keyCombination("Shift+Shortcut+S"));

        findAction = new Action(e -> content.getEditorPane().find());
        FXResourceBundle.getBundle().put(findAction.textProperty(), "find");
        FXResourceBundle.getBundle().put(findAction.longTextProperty(), "find");
        findAction.setAccelerator(KeyCombination.keyCombination("Shortcut+F"));

        goToLineAction = new Action(e -> content.getEditorPane().goToLine());
        FXResourceBundle.getBundle().put(goToLineAction.textProperty(), "goToLine");
        FXResourceBundle.getBundle().put(goToLineAction.longTextProperty(), "goToLine");
        goToLineAction.setAccelerator(KeyCombination.keyCombination("Shortcut+L"));

        showInFavoritesAction = new Action(e -> content.showInFavorites());
        FXResourceBundle.getBundle().put(showInFavoritesAction.textProperty(), "showInFavorites");
        FXResourceBundle.getBundle().put(showInFavoritesAction.longTextProperty(), "showInFavorites");
        showInFavoritesAction.setAccelerator(KeyCombination.keyCombination("Alt+Shift+W"));
    }

    private void setListeners() {
        saveAllAction.disabledProperty().bind(content.getEditorPane().changedProperty().not());

        content.getEditorPane().selectedEditorProperty().addListener((v, o, n) -> {
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

    Action findAction() {
        return findAction;
    }

    Action goToLineAction() {
        return goToLineAction;
    }

    Action showInFavoritesAction() {
        return showInFavoritesAction;
    }
}

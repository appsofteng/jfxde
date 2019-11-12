package dev.jfxde.sysapps.editor;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.Event;

import static javafx.scene.input.KeyCode.S;
import static javafx.scene.input.KeyCombination.CONTROL_DOWN;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;

import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;
import org.fxmisc.wellbehaved.event.Nodes;

public class EditorActions {

    private EditorContent content;
    private ReadOnlyBooleanWrapper saveDisable = new ReadOnlyBooleanWrapper(true);
    private ReadOnlyBooleanWrapper saveAllDisable = new ReadOnlyBooleanWrapper();

    public EditorActions(EditorContent content) {
        this.content = content;

        setListeners();
    }

    private void setListeners() {
        saveAllDisable.bind(content.getEditorPane().changedProperty().not());

        content.getEditorPane().selectedEditorProperty().addListener((v,o,n) -> {
            saveDisable.unbind();
            if (n != null) {
                saveDisable.bind(n.changedProperty().not());
            } else {
                saveDisable.set(true);
            }
        });

        Nodes.addInputMap(content.getEditorPane(), sequence(
                consume(keyPressed(S, CONTROL_DOWN), this::save),
                consume(keyPressed(S, SHIFT_DOWN, CONTROL_DOWN), this::saveAll)
            ));
    }

    ReadOnlyBooleanProperty saveDisableProperty() {
        return saveDisable.getReadOnlyProperty();
    }

    ReadOnlyBooleanProperty saveAllDisableProperty() {
        return saveAllDisable.getReadOnlyProperty();
    }

    void save(Event e) {
        content.getEditorPane().save();
    }

    void saveAll(Event e) {
        content.getEditorPane().saveAll();
    }
}

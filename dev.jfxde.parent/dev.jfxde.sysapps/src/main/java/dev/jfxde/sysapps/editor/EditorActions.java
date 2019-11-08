package dev.jfxde.sysapps.editor;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.event.Event;

public class EditorActions {

    private EditorContent content;
    private ReadOnlyBooleanWrapper saveDisable = new ReadOnlyBooleanWrapper(true);
    private ReadOnlyBooleanWrapper saveAllDisable = new ReadOnlyBooleanWrapper();

    public EditorActions(EditorContent content) {
        this.content = content;

        setListeners();
    }

    private void setListeners() {
        saveAllDisable.bind(content.getEditorPane().editedProperty().not());

        content.getEditorPane().selectedEditorProperty().addListener((v,o,n) -> {
            saveDisable.unbind();
            if (n != null) {
                saveDisable.bind(n.editedProperty().not());
            } else {
                saveDisable.set(true);
            }
        });
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

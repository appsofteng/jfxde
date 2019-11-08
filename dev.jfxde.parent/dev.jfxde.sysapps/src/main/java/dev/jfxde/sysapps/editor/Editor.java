package dev.jfxde.sysapps.editor;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jfxde.fxmisc.richtext.AreaUtils;
import dev.jfxde.fxmisc.richtext.CodeAreaExtender;
import dev.jfxde.logic.data.FXPath;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.StackPane;

public class Editor extends StackPane {

    private FXPath path;
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final CodeArea area = new CodeArea();

    public Editor(FXPath path) {
        this.path = path;

        title.bind(Bindings.createStringBinding(() -> path.getPath().toString(), path.pathProperty()));
        tabTitle.bind(Bindings.when(edited)
                .then(Bindings.createStringBinding(() -> "*" + path.getName(), path.nameProperty()))
                .otherwise(Bindings.createStringBinding(() -> path.getName(), path.nameProperty())));

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> setEdited((Boolean) n));
        area.textProperty().addListener((v, o, n) -> setEdited(true));

        CodeAreaExtender.get(area, path.getPath())
                .style()
                .highlighting()
                .indentation();

        getChildren().add(new VirtualizedScrollPane<>(area));

        AreaUtils.readText(area, path.getPath());
    }

    public FXPath getPath() {
        return path;
    }

    ReadOnlyStringProperty titleProperty() {
        return title.getReadOnlyProperty();
    }

    ReadOnlyStringProperty tabTitleProperty() {
        return tabTitle.getReadOnlyProperty();
    }

    boolean isEdited() {
        return edited.get();
    }

    private void setEdited(boolean value) {
        edited.set(value);
    }

    ReadOnlyBooleanProperty editedProperty() {
        return edited.getReadOnlyProperty();
    }

    void save() {
        if (isEdited()) {
            AreaUtils.writeText(area, path.getPath(), () -> setEdited(false));
        }
    }

    void dispose() {
        area.dispose();
    }
}

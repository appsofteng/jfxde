package dev.jfxde.sysapps.editor;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jfxde.fxmisc.richtext.CodeAreaExtender;
import dev.jfxde.j.util.LU;
import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.logic.data.FXFiles;
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
    private final ReadOnlyBooleanWrapper deleted = new ReadOnlyBooleanWrapper();
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
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

        setListeners();

        load();
    }

    private void setListeners() {
        path.getOnModified().add(p -> {
            setModified(true);
        });

        path.getOnDelete().add(p -> {
            System.out.println("to be deleted");
            return !isEdited();
        });

        path.getOnDeleted().add(p -> {
            System.out.println("deleted");
            setDeleted(true);
        });

        path.getOnDeletedExternally().add(p -> {
            System.out.println("deleted externally");
        });
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

    boolean isModified() {
        return modified.get();
    }

    private void setModified(boolean value) {
        modified.set(value);
    }

    ReadOnlyBooleanProperty modifiedProperty() {
        return modified.getReadOnlyProperty();
    }

    boolean isDeleted() {
        return deleted.get();
    }

    private void setDeleted(boolean value) {
        deleted.set(value);
    }

    ReadOnlyBooleanProperty deletedProperty() {
        return deleted.getReadOnlyProperty();
    }

    void load() {
        setModified(false);
        area.clear();
        CompletableFuture.supplyAsync(() -> LU.of(() -> Files.readString(path.getPath())))
        .thenAccept(s -> XPlatform.runFX(() -> {
            area.replaceText(0, 0, s);
            area.getUndoManager().forgetHistory();
            area.requestFocus();
            area.moveTo(0);
            area.requestFollowCaret();
        }));
    }

    void keep() {
        setModified(false);
        setEdited(true);
    }

    void save() {
        if (isEdited()) {
            FXFiles.save(path, area.getText()).thenRun(() -> XPlatform.runFX(() -> setEdited(false)));
        }
    }

    void dispose() {
        area.dispose();
    }
}

package dev.jfxde.sysapps.editor;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jfxde.fxmisc.richtext.CodeAreaWrappers;
import dev.jfxde.j.util.LU;
import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePointer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.StackPane;

public class Editor extends StackPane {

    private FilePointer filePointer;
    private FXPath path;
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper deleted = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper deletedExternally = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper changed = new ReadOnlyBooleanWrapper();
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final CodeArea area = new CodeArea();

    public Editor(FilePointer filePointer) {
        this.filePointer = filePointer;
        this.path = filePointer.getPath();

        title.bind(Bindings.createStringBinding(() -> path.getPath().toString(), path.pathProperty()));
        tabTitle.bind(Bindings.createStringBinding(() -> getTabString(), path.nameProperty(), edited, modified, deletedExternally));

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> setEdited((Boolean) n));
        area.textProperty().addListener((v, o, n) -> setEdited(true));

        CodeAreaWrappers.get(area, path.getPath())
                .style()
                .highlighting()
                .indentation()
                .find();

        getChildren().add(new VirtualizedScrollPane<>(area));

        setListeners();

        load();
    }

    private void setListeners() {
        changed.bind(edited.or(modified).or(deletedExternally));

        path.getOnModified().add(p -> {
            XPlatform.runFX(() -> { setModified(true); setDeletedExternally(false);});
        });

        path.getOnDelete().add(p -> {
            return !isEdited();
        });

        path.getOnDeleted().add(p -> {
            setDeleted(true);
        });

        path.getOnDeletedExternally().add(p -> {
            XPlatform.runFX(() -> { setDeletedExternally(true); setModified(false);});
        });
    }

    private String getTabString() {
        String str = "";

        if (isEdited()) {
            str += "*";
        }

        if (isModified()) {
            str += "!";
        }

        if (isDeletedExternally()) {
            str += "?";
        }

        str += path.getName();

        return str;
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

    private void setDeletedExternally(boolean value) {
        deletedExternally.set(value);
    }

    private boolean isDeletedExternally() {
        return deletedExternally.get();
    }

    ReadOnlyBooleanProperty deletedExternallyProperty() {
        return deletedExternally.getReadOnlyProperty();
    }

    boolean isChanged() {
        return changed.get();
    }

    void unchange() {
        setEdited(false);
        setModified(false);
        setDeletedExternally(false);
    }

    ReadOnlyBooleanProperty changedProperty() {
        return changed.getReadOnlyProperty();
    }

    void load() {
        CompletableFuture.supplyAsync(() -> LU.of(() -> Files.readString(path.getPath())))
                .thenAccept(s -> XPlatform.runFX(() -> {
                    area.clear();
                    area.replaceText(0, 0, s);

                    area.getUndoManager().forgetHistory();
                    area.requestFocus();
                    area.moveTo(0);

                    area.requestFollowCaret();
                    setEdited(false);
                    setModified(false);
                }));
    }

    void save() {
        if (isChanged()) {
            FXFiles.save(path, area.getText()).thenRun(() -> XPlatform.runFX(() -> unchange()));
        }
    }

    void dispose() {
        area.dispose();
    }
}

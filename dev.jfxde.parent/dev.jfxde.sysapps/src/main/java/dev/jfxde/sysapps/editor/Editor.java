package dev.jfxde.sysapps.editor;

import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.controlsfx.control.action.ActionUtils;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jfxde.fxmisc.richtext.CodeAreaWrappers;
import dev.jfxde.fxmisc.richtext.ContextMenuBuilder;
import dev.jfxde.j.util.LU;
import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePosition;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.scene.layout.StackPane;

public class Editor extends StackPane {

    private FilePosition filePosition;
    private FXPath path;
    private final ReadOnlyBooleanWrapper edited = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper modified = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper deleted = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper deletedExternally = new ReadOnlyBooleanWrapper();
    private final ReadOnlyBooleanWrapper changed = new ReadOnlyBooleanWrapper();
    private final ReadOnlyStringWrapper title = new ReadOnlyStringWrapper();
    private final ReadOnlyStringWrapper tabTitle = new ReadOnlyStringWrapper();
    private final CodeArea area = new CodeArea();

    private CodeAreaWrappers codeAreaWrappers;

    public Editor(FilePosition filePosition, EditorActions actions) {
        setFilePosition(filePosition);

        title.bind(Bindings.createStringBinding(() -> getPath().getPath().toString(), getPath().pathProperty()));
        tabTitle.bind(Bindings.createStringBinding(() -> getTabString(), getPath().nameProperty(), edited, modified, deletedExternally));

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));
        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> setEdited((Boolean) n));
        area.textProperty().addListener((v, o, n) -> setEdited(true));

        ContextMenuBuilder.get(area)
                .addAll(ActionUtils.createMenuItem(actions.saveAction()), ActionUtils.createMenuItem(actions.saveAllAction()))
                .separator()
                .add(ActionUtils.createMenuItem(actions.findAction()))
                .add(ActionUtils.createMenuItem(actions.goToLineAction()))
                .add(ActionUtils.createMenuItem(actions.showInFavoritesAction()))
                .separator()
                .copy().cut().paste().selectAll().clear()
                .separator()
                .undo().redo();

        codeAreaWrappers = CodeAreaWrappers.get(area, path.getPath())
                .style()
                .highlighting()
                .indentation()
                .find();

        getChildren().addAll(new VirtualizedScrollPane<>(area));

        setListeners();

        load();
    }

    private void setListeners() {
        changed.bind(edited.or(modified).or(deletedExternally));

        path.getOnModified().add(p -> {
            XPlatform.runFX(() -> {
                setModified(true);
                setDeletedExternally(false);
            });
        });

        path.getOnDelete().add(p -> {
            return !isEdited();
        });

        path.getOnDeleted().add(p -> {
            setDeleted(true);
        });

        path.getOnDeletedExternally().add(p -> {
            XPlatform.runFX(() -> {
                setDeletedExternally(true);
                setModified(false);
            });
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

    FXPath getPath() {
        return path;
    }

    CodeArea getArea() {
        return area;
    }

    CodeAreaWrappers getCodeAreaWrappers() {
        return codeAreaWrappers;
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

    private void setFilePosition(FilePosition filePosition) {
        this.filePosition = filePosition;
        this.path = filePosition.getPath();
    }

    void moveToPotition(FilePosition filePointer) {
        setFilePosition(filePointer);
        moveToPosition();
    }

    private void moveToPosition() {

        area.requestFocus();
        var stringPointer = filePosition.getSelectedPosition();

        if (stringPointer != null) {
            area.moveTo(stringPointer.getStringRef().getLine().getNumber(), stringPointer.getStringRef().getStart());
        } else {
            area.moveTo(0);
        }

        area.requestFollowCaret();
    }

    void load() {
        CompletableFuture.supplyAsync(() -> LU.of(() -> Files.readString(path.getPath())))
                .thenAccept(s -> XPlatform.runFX(() -> {
                    area.clear();
                    area.replaceText(0, 0, s);

                    area.getUndoManager().forgetHistory();

                    moveToPosition();

                    setEdited(false);
                    setModified(false);
                }));
    }

    void save() {
        if (isChanged()) {
            FXFiles.save(path, area.getText()).thenRun(() -> XPlatform.runFX(() -> unchange()));
        }
    }

    void goToLine(int line) {

        line = Math.min(Math.max(line, 1),getArea().getParagraphs().size());

        getArea().moveTo(line - 1, 0);
    }

    void dispose() {
        area.dispose();
    }
}

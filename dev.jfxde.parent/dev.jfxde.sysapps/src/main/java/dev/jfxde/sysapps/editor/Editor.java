package dev.jfxde.sysapps.editor;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletableFuture;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.jfxext.util.FXUtils;
import dev.jfxde.jfxext.util.LU;
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
                .then(Bindings.createStringBinding(() -> "*" + path.getName(), path.pathProperty()))
                .otherwise(Bindings.createStringBinding(() -> path.getName(), path.pathProperty())));

        area.setParagraphGraphicFactory(LineNumberFactory.get(area));

        getChildren().add(new VirtualizedScrollPane<>(area));

        readText();
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

    private void setEdited(boolean value) {
        edited.set(value);
    }

    ReadOnlyBooleanProperty editedProperty() {
        return edited.getReadOnlyProperty();
    }

    private void readText() {

        CompletableFuture.supplyAsync(() -> LU.of(() -> Files.readString(path.getPath())))
                .thenAccept(s ->
                    XPlatform.runFX(() -> {
                        area.replaceText(0, 0, s);
                        area.getUndoManager().forgetHistory();
                        area.getUndoManager().undoAvailableProperty().addListener((v, o, n) -> setEdited((Boolean) n));
                        area.textProperty().addListener((v, o, n) -> setEdited(true));
                        area.requestFocus();
                        area.moveTo(0);
                        area.requestFollowCaret();
                    })
               );

    }
}

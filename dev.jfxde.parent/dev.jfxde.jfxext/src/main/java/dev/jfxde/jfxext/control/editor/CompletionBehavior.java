package dev.jfxde.jfxext.control.editor;

import java.util.Collection;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class CompletionBehavior<T extends GenericStyledArea<?,?,?>> extends Behavior<T> {

    private CompletionPopup codeCompletion;
    private Consumer<CompletionBehavior<T>> complete;
    private Function<String, String> documentation;

    public CompletionBehavior(Consumer<CompletionBehavior<T>> complete, Function<String, String> documentation) {
        this.complete = complete;
        this.documentation = documentation;
    }


    protected void setBehavior() {
        area.getStylesheets().add(getClass().getResource("completion.css").toExternalForm());
        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
                codeCompletion();
            }
        });

        area.caretPositionProperty().addListener((v, o, n) -> {
            if (codeCompletion != null) {
                codeCompletion();
            }
        });
    }

    private void codeCompletion() {

        if (codeCompletion != null) {
            codeCompletion.close();
        }

        complete.accept(this);
    }

    public void showCompletionItems(Collection<CompletionItem> items) {
        codeCompletion = new CompletionPopup(items, documentation);

        Optional<Bounds> boundsOption = area.caretBoundsProperty().getValue();

        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            codeCompletion.show(area, bounds.getMaxX(), bounds.getMaxY());
            codeCompletion.setOnHidden(ev -> {
                CompletionItem selection = codeCompletion.getSelection();
                codeCompletion = null;
                if (selection != null) {
                    selection.complete();
                }
            });
        }
    }
}

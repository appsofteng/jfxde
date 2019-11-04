package dev.jfxde.fxmisc.richtext;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.fxmisc.richtext.extensions.CompletionItem;
import dev.jfxde.fxmisc.richtext.extensions.CompletionPopup;
import dev.jfxde.fxmisc.richtext.extensions.DocRef;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class CodeAreaExtender {

    private CodeArea area;

    private CodeAreaExtender(CodeArea area) {
        this.area = area;
    }

    public static CodeAreaExtender get(CodeArea area) {
        return new CodeAreaExtender(area);
    }

    public CodeAreaExtender style(String language) {
        area.getStylesheets().add(CodeAreaExtender.class.getResource(language + ".css").toExternalForm());
        return this;
    }

    public CodeAreaExtender highlighting(String language, AtomicBoolean disableHighlight) {
        style(language);
        Lexer lexer = Lexer.get(language);
        area.richChanges()
                .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
                .successionEnds(Duration.ofMillis(200))
                .subscribe(ch -> {
                    if (disableHighlight.get()) {
                        return;
                    }
                    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

                    var end = lexer.tokenize(area.getText(), (lastEnd, t) -> {
                        spansBuilder.add(Collections.emptyList(), t.getStart() - lastEnd);
                        spansBuilder.add(new StyleSpan<>(List.of(t.getType().toLowerCase()), t.getLength()));
                    });

                    spansBuilder.add(Collections.emptyList(), area.getText().length() - end);

                    StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
                    area.setStyleSpans(0, styleSpans);

                });

        return this;
    }

    public CodeAreaExtender completion(Consumer<Consumer<Collection<CompletionItem>>> complete, Function<DocRef, String> documentation) {
        area.getStylesheets().add(getClass().getResource("completion.css").toExternalForm());
        CompletionPopup codeCompletion = new CompletionPopup(documentation);

        Consumer<Collection<CompletionItem>> show = items -> {
            Optional<Bounds> boundsOption = area.caretBoundsProperty().getValue();
            if (boundsOption.isPresent()) {
                Bounds bounds = boundsOption.get();
                codeCompletion.setItems(items);
                codeCompletion.show(area, bounds.getMaxX(), bounds.getMaxY());
            }
        };

        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
                complete.accept(show);
            }
        });

        area.caretPositionProperty().addListener((v, o, n) -> {
            if (codeCompletion != null && codeCompletion.isShowing()) {
                complete.accept(show);
            }
        });

        return this;
    }
}

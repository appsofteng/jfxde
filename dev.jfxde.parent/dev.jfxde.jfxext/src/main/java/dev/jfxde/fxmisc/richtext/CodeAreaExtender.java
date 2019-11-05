package dev.jfxde.fxmisc.richtext;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.fxmisc.wellbehaved.event.Nodes;

import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class CodeAreaExtender {

    private CodeArea area;
    private String language;
    private Lexer lexer;

    private CodeAreaExtender(CodeArea area, String language) {
        this.area = area;
        this.language = language;
    }

    public static CodeAreaExtender get(CodeArea area, String language) {
        return new CodeAreaExtender(area, language);
    }

    private Lexer getLexer() {

        if (lexer == null) {
            lexer = Lexer.get(language);
        }

        return lexer;
    }

    public CodeAreaExtender style() {
        area.getStylesheets().add(CodeAreaExtender.class.getResource(language + ".css").toExternalForm());
        return this;
    }

    public CodeAreaExtender highlighting(AtomicBoolean disableHighlight) {
        style();
        area.getStylesheets().add(CodeAreaExtender.class.getResource(language + "-edit.css").toExternalForm());

        BlockEndWrapper<GenericStyledArea<?,?,?>> blockEndWrapper = new BlockEndWrapper<>(area);

        area.richChanges()
                .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
                .successionEnds(Duration.ofMillis(100))
                .subscribe(ch -> {
                    if (disableHighlight.get()) {
                        return;
                    }
                    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

                    var end = getLexer().tokenize(area.getText(), ch.getPosition(), (lastEnd, t) -> {
                        spansBuilder.add(Collections.emptyList(), t.getStart() - lastEnd);
                        spansBuilder.add(new StyleSpan<>(List.of(t.getType().toLowerCase()), t.getLength()));
                    });

                    spansBuilder.add(Collections.emptyList(), area.getText().length() - end);

                    StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
                    area.setStyleSpans(0, styleSpans);
                    blockEndWrapper.indentEnd(getLexer().getCloseTokenOnPosition());

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

    public CodeAreaExtender indentation() {
        IndentationWrapper<GenericStyledArea<?, ?, ?>> indentationWrapper = new IndentationWrapper<>(area, getLexer());
        Nodes.addInputMap(area, sequence(
                consume(keyPressed(ENTER), e -> indentationWrapper.insertNewLineIndentation()),
                consume(keyPressed(TAB), e -> indentationWrapper.insertIndentation()),
                consume(keyPressed(TAB, SHIFT_DOWN), e -> indentationWrapper.deleteIndentation())));

        return this;
    }
}

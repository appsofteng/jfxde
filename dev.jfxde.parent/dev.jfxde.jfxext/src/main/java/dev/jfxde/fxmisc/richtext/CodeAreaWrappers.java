package dev.jfxde.fxmisc.richtext;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
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

import dev.jfxde.j.nio.file.XFiles;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class CodeAreaWrappers {

    private CodeArea area;
    private String fileName;
    private String language;
    private Lexer lexer;

    private CodeAreaWrappers(CodeArea area, String fileName, String language) {
        this.area = area;
        this.fileName = fileName;
        this.language = language;
    }

    public static CodeAreaWrappers get(CodeArea area, String language) {
        return new CodeAreaWrappers(area, language, language);
    }

    public static CodeAreaWrappers get(CodeArea area, Path path) {
        String fileName = path.getFileName().toString().toLowerCase();
        return new CodeAreaWrappers(area, fileName, XFiles.getFileExtension(fileName));
    }

    private Lexer getLexer() {

        if (lexer == null) {
            lexer = Lexer.get(fileName, language);
        }

        return lexer;
    }

    public CodeAreaWrappers style() {
        var languegeSyle = CodeAreaWrappers.class.getResource(language + ".css");
        if (languegeSyle != null) {
            area.getStylesheets().add(languegeSyle.toExternalForm());
        }
        area.getStylesheets().add(CodeAreaWrappers.class.getResource("area.css").toExternalForm());

        if (area.isEditable()) {
            area.getStylesheets().add(CodeAreaWrappers.class.getResource("edit.css").toExternalForm());
        }

        return this;
    }

    public CodeAreaWrappers highlighting() {
        return highlighting(new AtomicBoolean());
    }

    public CodeAreaWrappers highlighting(AtomicBoolean disableHighlight) {

        if (getLexer() == null) {
            return this;
        }

        var blockEndWrapper = new BlockEndWrapper<>(area);
        var highlightWrapper = new HighlightWrapper(area, getLexer());

        area.richChanges()
                .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
                .successionEnds(Duration.ofMillis(100))
                .subscribe(ch -> {
                    if (disableHighlight.get()) {
                        return;
                    }

                    int insertionEnd = ch.toPlainTextChange().getInsertionEnd();
                    int caretPosition = insertionEnd >= 0 ? insertionEnd : area.getCaretPosition();

                    // Use List not StyleSpansBuilder, StyleSpansBuilder merges styles immediately.
                    List<StyleSpan<Collection<String>>> spans = new ArrayList<>();

                    var end = getLexer().tokenize(area.getText(), caretPosition, (lastEnd, t) -> {
                        spans.add(new StyleSpan<>(Collections.emptyList(), t.getStart() - lastEnd));
                        spans.add(highlightWrapper.getStyleSpan(t));
                    });

                    StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
                    spansBuilder.addAll(spans);
                    spansBuilder.add(Collections.emptyList(), area.getText().length() - end);
                    StyleSpans<Collection<String>> styleSpans = spansBuilder.create();

                    area.setStyleSpans(0, styleSpans);
               //     blockEndWrapper.indentEnd(getLexer().getCloseTokenOnChangePosition());

                });

        area.caretPositionProperty().addListener((v, o, n) -> {
            Platform.runLater(() -> highlightWrapper.highlightDelimiters(n));
        });

        return this;
    }

    public CodeAreaWrappers completion(Consumer<Consumer<Collection<CompletionItem>>> complete, Function<DocRef, String> documentation) {
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

    public CodeAreaWrappers indentation() {
        IndentationWrapper<GenericStyledArea<?, ?, ?>> indentationWrapper = new IndentationWrapper<>(area, getLexer());
        Nodes.addInputMap(area, sequence(
                consume(keyPressed(ENTER), e -> indentationWrapper.insertNewLineIndentation()),
                consume(keyPressed(TAB), e -> indentationWrapper.insertIndentation()),
                consume(keyPressed(TAB, SHIFT_DOWN), e -> indentationWrapper.deleteIndentation())));

        return this;
    }

    public CodeAreaWrappers find() {

        var findWrapper = new FindWrapper(area);

        Nodes.addInputMap(area, sequence(
                consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> findWrapper.findWord())));

        return this;
    }
}

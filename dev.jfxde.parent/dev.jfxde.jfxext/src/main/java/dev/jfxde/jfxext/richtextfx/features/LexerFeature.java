package dev.jfxde.jfxext.richtextfx.features;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.RichTextChange;

public class LexerFeature<T extends StyleClassedTextArea> extends Feature<T> {

    private Lexer lexer;
    final List<Consumer<RichTextChange<?, ?, ?>>> richTextChangeConsumers = new ArrayList<>();

    private AtomicBoolean disableHighlight = new AtomicBoolean();

    public LexerFeature(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    public void init() {
        area.getStylesheets().addAll(lexer.getCss(), lexer.getCssEdit());
        area.richChanges()
                .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
                .successionEnds(Duration.ofMillis(200))
                .subscribe(ch -> {
                    richTextChangeConsumers.forEach(con -> con.accept(ch));
                    if (!disableHighlight.get()) {
                        getArea().setStyleSpans(0, getLexer().getStyleSpans(getArea().getText()));
                    }
                });
    }

    public Lexer getLexer() {
        return lexer;
    }

    public void setDisableHighlight(AtomicBoolean disableHighlight) {
        this.disableHighlight = disableHighlight;
    }

    void addRichTextChangeConsumer(Consumer<RichTextChange<?, ?, ?>> consumer) {
        richTextChangeConsumers.add(consumer);
    }
}

package dev.jfxde.jfxext.control.editor;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.model.RichTextChange;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class LexerFeature<T extends GenericStyledArea<?,?,?>> extends Feature<T> {

    private Lexer lexer;
    final List<Consumer<RichTextChange<?, ?, ?>>> richTextChangeConsumers = new ArrayList<>();

    public LexerFeature(Lexer lexer) {
        this.lexer = lexer;
    }

    @Override
    protected void init() {
        area.getStylesheets().add(getClass().getResource(lexer.getCss()).toExternalForm());
        area.richChanges()
        .filter(ch -> !ch.toPlainTextChange().getInserted().equals(ch.toPlainTextChange().getRemoved()))
        .successionEnds(Duration.ofMillis(200))
        .subscribe(ch -> {
            richTextChangeConsumers.forEach(con -> con.accept(ch));
        });
    }

    public Lexer getLexer() {
        return lexer;
    }

    void addRichTextChangeConsumer(Consumer<RichTextChange<?,?,?>> consumer) {
        richTextChangeConsumers.add(consumer);
    }
}

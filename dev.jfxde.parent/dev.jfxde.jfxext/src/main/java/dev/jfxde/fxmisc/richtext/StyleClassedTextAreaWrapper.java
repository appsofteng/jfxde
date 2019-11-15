package dev.jfxde.fxmisc.richtext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import javafx.scene.control.IndexRange;
import javafx.util.Pair;

public class StyleClassedTextAreaWrapper extends GenericStyledAreaWrapper<StyleClassedTextArea> {

    public StyleClassedTextAreaWrapper(StyleClassedTextArea area) {
        super(area);
    }

    void addStyleClass(IndexRange range, String styleClass) {
        changeStyleClass(range, styleClass, (s, c) -> s.add(c));
    }

    void removeStyleClass(IndexRange range, String styleClass) {
        changeStyleClass(range, styleClass, (s, c) -> s.remove(c));
    }

    private void changeStyleClass(IndexRange range, String styleClass, BiConsumer<Collection<String>,String> change) {
        StyleSpans<Collection<String>> styleSpans = getArea().getStyleSpans(range);

        var builder = new StyleSpansBuilder<Collection<String>>();
        styleSpans.stream().forEach(s -> {
            var style = new ArrayList<String>(s.getStyle());
            change.accept(style, styleClass);
            builder.add(style, s.getLength());
        });

        getArea().setStyleSpans(range.getStart(), builder.create());
    }

    Pair<IndexRange, String> getCodeWord(String code, int position) {

        StringBuilder word = new StringBuilder();
        int start = getCodeWordPart(code, position - 1, -1, c -> word.insert(0, c));
        int end = getCodeWordPart(code, position, 1, c -> word.append(c));

        return new Pair<>(new IndexRange(start, end), word.toString());
    }

    private int getCodeWordPart(String code, int position, int step, Consumer<Character> word) {
        int i = position;

        while (i >= 0 && i < code.length()) {
            char c = code.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                word.accept(c);
            } else {
                if (step < 0) {
                    i -= step;
                }
                break;
            }

            i += step;
        }

        if (i < 0) {
            i = 0;
        }

        return i;
    }
}

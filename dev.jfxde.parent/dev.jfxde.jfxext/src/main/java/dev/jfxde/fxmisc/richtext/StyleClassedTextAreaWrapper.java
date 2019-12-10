package dev.jfxde.fxmisc.richtext;

import java.util.Collection;
import java.util.HashSet;
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

    void addStyle(int start, int end, Collection<String> styleClasses) {
        changeStyleClass(start, end, styleClasses, (s, c) -> s.addAll(c));
    }

    void removeStyle(int start, int end, Collection<String> styleClasses) {
        changeStyleClass(start, end, styleClasses, (s, c) -> s.removeAll(c));
    }

    private void changeStyleClass(int start, int end, Collection<String> styleClasses, BiConsumer<Collection<String>,Collection<String>> change) {
        StyleSpans<Collection<String>> styleSpans = getArea().getStyleSpans(start, end);

        var builder = new StyleSpansBuilder<Collection<String>>();
        styleSpans.stream().forEach(s -> {
            var style = new HashSet<>(s.getStyle());
            change.accept(style, styleClasses);
            builder.add(style, s.getLength());
        });

        getArea().setStyleSpans(start, builder.create());
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

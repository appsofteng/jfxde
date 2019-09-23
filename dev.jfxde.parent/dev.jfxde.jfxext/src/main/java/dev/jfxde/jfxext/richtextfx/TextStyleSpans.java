package dev.jfxde.jfxext.richtextfx;

import java.util.Collection;
import java.util.Collections;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public class TextStyleSpans {

    private String text;
    private StyleSpans<Collection<String>> styleSpans;

    public TextStyleSpans(String text) {
        this(text, null);
    }

    public TextStyleSpans(String text, StyleSpans<Collection<String>> styleSpans) {
        this.text = text;

        if (styleSpans == null) {
            StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
            spansBuilder.add(Collections.emptyList(), text.length());
            styleSpans = spansBuilder.create();
        }

        this.styleSpans = styleSpans;
    }

    public String getText() {
        return text;
    }

    public StyleSpans<Collection<String>> getStyleSpans() {
        return styleSpans;
    }
}

package dev.jfxde.fxmisc.richtext.extensions;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.model.StyleSpan;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

public abstract class Lexer {

    private static final String INDENTATION = "    ";
    private List<TokenListener> tokenListeners = new ArrayList<>();
    protected String skipPattern;


    public static Lexer get(String fileName) {
        return get(fileName, null);
    }

    public static Lexer get(String fileName, String skipPattern) {
        Lexer lexer = null;
        int i = fileName.lastIndexOf(".");

        if (i > 0) {
            String extension = fileName.substring(i + 1);

            if ("java".equalsIgnoreCase(extension)) {
                lexer = JavaLexer.getJava(fileName, skipPattern);
            }
        }

        return lexer;
    }

    String getIndentation() {
        return INDENTATION;
    }

    void addListener(TokenListener listener) {
        tokenListeners.add(listener);
    }

    StyleSpans<Collection<String>> getStyleSpans(String text) {
        // Use list instead of style span builder because the builder puts the same styles together and later
        // they cannot be changed e.g. {{} leads to all braces being highlighted though only the last two match together.
        List<StyleSpan<Collection<String>>> spans = new ArrayList<>();

        Token token = new Token(text, getPattern(), this::getToken);
        tokenize(token, spans);

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        spansBuilder.addAll(spans);
        StyleSpans<Collection<String>> styleSpans = spansBuilder.create();

        return styleSpans;
    }

    private void tokenize(Token token, List<StyleSpan<Collection<String>>> spans) {
        int previousEnd = 0;
        Matcher matcher = token.getPattern().matcher(token.getValue());

        tokenListeners.forEach(c -> c.onLevelIncreased());

        while (matcher.find()) {
            Token subtoken = token.getTokenFunction().apply(matcher, token.getStyleClass());
            subtoken.setStart(token.getStart() + matcher.start());
            subtoken.setEnd(token.getStart() + matcher.end());

            spans.add(new StyleSpan<>(token.getStyle(), matcher.start() - previousEnd));

            if (subtoken.getPattern() == null) {
                tokenListeners.forEach(c -> c.process(subtoken));
                spans.add(new StyleSpan<>(subtoken.getStyle(), matcher.end() - matcher.start()));


            } else {
                tokenize(subtoken, spans);
            }

            previousEnd = matcher.end();
        }

        tokenListeners.forEach(c -> c.onLevelDecreased());
        spans.add(new StyleSpan<>(token.getStyle(), token.getValue().length() - previousEnd));

    }

    public abstract String getCss();
    abstract String getCssEdit();
    abstract Pattern getPattern();
    abstract boolean isDelimiter(String str);
    abstract boolean isOpeningDelimiter(String str);
    abstract boolean isClosingDelimiter(String str);
    abstract String getOpeningDelimitersPattern();
    abstract String getOpeningDelimiter(String closingDelimiter);
    abstract String getSingleLineComment();
    abstract Token getToken(Matcher matcher, String defaultStyleClass);
}

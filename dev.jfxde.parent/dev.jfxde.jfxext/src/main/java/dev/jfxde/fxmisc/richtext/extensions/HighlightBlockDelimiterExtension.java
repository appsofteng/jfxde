package dev.jfxde.fxmisc.richtext.extensions;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.fxmisc.richtext.TextEditingArea;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.beans.value.ObservableValue;

import javafx.application.Platform;

import org.fxmisc.richtext.CodeArea;

public class HighlightBlockDelimiterExtension<T extends StyleClassedTextArea> extends AreaExtension<T> implements TokenListener {

    private LexerExtension<StyleClassedTextArea> lexerFeature;
    private boolean delimiterFound;
    private int insertionEnd = -1;
    private Deque<Map<String, Deque<Token>>> metaStack = new ArrayDeque<>();
    private Map<String, Deque<Token>> stacks = Collections.emptyMap();

    @Override
    public void init() {

        lexerFeature = areaExtensions.getExtension(LexerExtension.class);
        lexerFeature.addRichTextChangeConsumer(ch -> {
            insertionEnd = ch.toPlainTextChange().getInsertionEnd();
            delimiterFound = false;
        });

        lexerFeature.getLexer().addListener(this);

        getArea().caretPositionProperty().addListener(this::highlightBlockDelimiters);
    }

    private void highlightBlockDelimiters(ObservableValue<? extends Integer> observable, int oldValue, int newValue) {

        if (newValue == insertionEnd) {
            insertionEnd = -1;
            return;
        }

        insertionEnd = -1;

        boolean isDelimiterAfter = false;
        int delimiterIndexAfter = 0;
        String textAfter = "";

        boolean isDelimiterBefore = false;
        int delimiterIndexBefore = 0;
        String textBefore = "";

        if (newValue < getArea().getText().length()) {
            delimiterIndexAfter = newValue;
            textAfter = getArea().getText(delimiterIndexAfter, delimiterIndexAfter + 1);

            isDelimiterAfter = lexerFeature.getLexer().isDelimiter(textAfter);
            if (newValue > 0) {
                delimiterIndexBefore = newValue - 1;
                textBefore = getArea().getText(delimiterIndexBefore, delimiterIndexBefore + 1);
                isDelimiterBefore = lexerFeature.getLexer().isDelimiter(textBefore);
            }
        }

        Collection<String> delimiterStyleAfter = getArea().getStyleOfChar(delimiterIndexAfter);
        Collection<String> delimiterStyleBefore = getArea().getStyleOfChar(delimiterIndexBefore);

        if (isDelimiterAfter && delimiterStyleAfter.contains("block-delimiter-match") && !isDelimiterBefore
                || isDelimiterBefore && delimiterStyleBefore.contains("block-delimiter-match") && !isDelimiterAfter) {
            return;
        }

        if (lexerFeature.getLexer().isOpeningDelimiter(textAfter) && lexerFeature.getLexer().isOpeningDelimiter(textBefore)
                && delimiterStyleAfter.contains("block-delimiter-match")
                || lexerFeature.getLexer().isClosingDelimiter(textAfter) && lexerFeature.getLexer().isClosingDelimiter(textBefore)
                        && delimiterStyleBefore.contains("block-delimiter-match")) {
            return;
        }

        if (isDelimiterAfter || isDelimiterBefore || delimiterFound) {
            delimiterFound = false;
            Platform.runLater(() -> {
                getArea().setStyleSpans(0, lexerFeature.getLexer().getStyleSpans(getArea().getText()));
            });
        }
    }

    public void onLevelIncreased() {
        metaStack.push(stacks);
        stacks = new HashMap<>();
    }

    public void onLevelDecreased() {
        stacks = metaStack.pop();
    }

    public void process(Token token) {

        if (lexerFeature.getLexer().isOpeningDelimiter(token.getValue())) {
            Deque<Token> stack = stacks.computeIfAbsent​(token.getValue(), k -> new ArrayDeque<>());
            stack.push(token);
        } else if (lexerFeature.getLexer().isClosingDelimiter(token.getValue())) {
            Deque<Token> stack = stacks.get​(lexerFeature.getLexer().getOpeningDelimiter(token.getValue()));
            if (stack != null && !stack.isEmpty()) {
                Token open = stack.pop();

                if (!delimiterFound && (isCaretPosition(open.getStart(), insertionEnd) || isCaretPosition(token.getStart(), insertionEnd))) {

                    open.addStyleClass("block-delimiter-match");
                    token.addStyleClass("block-delimiter-match");
                    delimiterFound = true;
                }
            }
        }
    }
}

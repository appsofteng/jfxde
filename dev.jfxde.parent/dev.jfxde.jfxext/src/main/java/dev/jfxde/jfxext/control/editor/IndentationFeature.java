package dev.jfxde.jfxext.control.editor;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.TAB;
import static javafx.scene.input.KeyCombination.SHIFT_DOWN;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.wellbehaved.event.Nodes;

import javafx.scene.control.IndexRange;

public class IndentationFeature<T extends GenericStyledArea<?,?,?>> extends Feature<T> {

    private static final String INDENTATION = "    ";

    @Override
    protected void init() {
        Nodes.addInputMap(getArea(), sequence(
                consume(keyPressed(ENTER), e -> insertNewLineIndentation()),
                consume(keyPressed(TAB), e -> insertIndentation()),
                consume(keyPressed(TAB, SHIFT_DOWN), e -> deleteIndentation())
            ));
    }

    String getIndentation() {
        return INDENTATION;
    }

    void insertNewLineIndentation() {

        String paragraph = getCurrentParagraphText();

        String indentation = getParagraphIndentation(getArea().getCurrentParagraph());

        if (paragraph.matches(".*" + getOpeningDelimitersPattern() + " *$")) {
            indentation += getIndentation();
        }

        if (paragraph.trim().isEmpty()  && getArea().getCaretColumn() < indentation.length()) {
            indentation = "";
        }

        getArea().insertText(getArea().getCaretPosition(), "\n" + indentation);
    }

    private String getOpeningDelimitersPattern() {
        return editor.getFeature(LexerFeature.class) != null ? editor.getFeature(LexerFeature.class).getLexer().getOpeningDelimitersPattern() : "";
    }

    void insertIndentation() {
        IndexRange selectionRange = getArea().getSelection();

        if (selectionRange.getLength() == 0) {
            insertIndentationForCaret();
        } else {
            changeParagraphs(selectionRange, this::insertIndentationForParagraph);
        }
    }

    void deleteIndentation() {
        IndexRange selectionRange = getArea().getSelection();
        if (selectionRange.getLength() == 0) {
           deleteIndentationForCaret();
        } else {
            changeParagraphs(selectionRange, this::deleteIndentationForParagraph);
        }
    }

    private void insertIndentationForCaret() {

        if (isCaretInIndentation()) {
            int spaceCount = getCurrentParagraphIndentation().length() % getIndentation().length();
            getArea().insertText(getArea().getCaretPosition(), getIndentation().substring(spaceCount));
        } else {
            getArea().insertText(getArea().getCaretPosition(), getIndentation());
        }
    }

    private String insertIndentationForParagraph(int paragraphIndex) {
        int spaceCount = getParagraphIndentation(paragraphIndex).length() % getIndentation().length();
        String text = getIndentation().substring(spaceCount) + getArea().getText(paragraphIndex);

        return text;
    }

    private void deleteIndentationForCaret() {

        if (getArea().getCaretColumn() == 0) {
            getArea().moveTo(getArea().getCurrentParagraph(), getCurrentParagraphIndentation().length());
        }

        int spaceCount = getArea().getCaretColumn() % getIndentation().length();
        if (spaceCount == 0) {
            spaceCount = getIndentation().length();
        }

        if (isCaretInIndentation()) {
            getArea().deleteText(getArea().getCaretPosition() - spaceCount, getArea().getCaretPosition());
        } else {
            getArea().moveTo(getArea().getCaretPosition() - spaceCount);
        }
    }

    private String deleteIndentationForParagraph(int paragraphIndex) {
        int paragraphIndentationLength = getParagraphIndentation(paragraphIndex).length();
        String text = getArea().getText(paragraphIndex);

        if (paragraphIndentationLength == 0) {
            return text;
        }

        int spaceCount = paragraphIndentationLength % getIndentation().length();

        if (spaceCount == 0) {
            spaceCount = getIndentation().length();
        }

        text = text.substring(spaceCount);

        return text;
    }
}

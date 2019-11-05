package dev.jfxde.fxmisc.richtext;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.scene.control.IndexRange;

public class IndentationWrapper<T extends GenericStyledArea<?,?,?>> extends AreaWrapper<T> {

    private Lexer lexer;

    IndentationWrapper(T area, Lexer lexer) {
        super(area);
        this.lexer = lexer;
    }

    private static final String INDENTATION = "    ";

    String getIndentation() {
        return INDENTATION;
    }

    void insertNewLineIndentation() {

        String paragraph = getCurrentParagraphText();

        String indentation = getParagraphIndentation(getArea().getCurrentParagraph());

        if (paragraph.substring(0, area.getCaretColumn()).matches(".*" + lexer.getOpenTokenPattern() + " *$")) {
            indentation += getIndentation();
        }

        if (paragraph.trim().isEmpty() && getArea().getCaretColumn() < indentation.length()) {
            indentation = "";
        }

        getArea().insertText(getArea().getCaretPosition(), "\n" + indentation);
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

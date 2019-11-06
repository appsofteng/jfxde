package dev.jfxde.fxmisc.richtext;

import java.util.Collection;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpan;

public class HighlightWrapper extends AreaWrapper<CodeArea> {

    private Token token;
    private Lexer lexer;
    private int areaLength;

    public HighlightWrapper(CodeArea area, Lexer lexer) {
        super(area);
        this.lexer = lexer;
        this.areaLength = area.getLength();
    }

    void highlightDelimiters(int caretPosition) {

        if (area.getLength() != areaLength) {
            areaLength = area.getLength();

            return;
        }

        if (token != null) {
            removeHighlightDelimiter();
        }

        token = lexer.getToken(caretPosition);
        if (token != null) {
            highlightDelimiter();
        }
    }

    private void highlightDelimiter() {
        if (token.isDelimiter()) {
            token.getStyle().add("block-delimiter-match");
            token.getOppositeToken().getStyle().add("block-delimiter-match");
            area.setStyle(token.getStart(), token.getEnd(), token.getStyle());
            area.setStyle(token.getOppositeToken().getStart(), token.getOppositeToken().getEnd(), token.getOppositeToken().getStyle());
        }
    }

    private void removeHighlightDelimiter() {
        if (token.isDelimiter()) {
            token.getStyle().remove("block-delimiter-match");
            token.getOppositeToken().getStyle().remove("block-delimiter-match");
            area.setStyle(token.getStart(), token.getEnd(), token.getStyle());
            area.setStyle(token.getOppositeToken().getStart(), token.getOppositeToken().getEnd(), token.getOppositeToken().getStyle());
        }
    }

    StyleSpan<Collection<String>> getStyleSpan(Token token) {
        StyleSpan<Collection<String>> styleSpan = new StyleSpan<>(token.getStyle(), token.getLength());

        if (token.isCloseOnCaretPosition()) {
            token.getStyle().add("block-delimiter-match");
            token.getOppositeToken().getStyle().add("block-delimiter-match");
            this.token = token;
        }

        return styleSpan;
    }
}

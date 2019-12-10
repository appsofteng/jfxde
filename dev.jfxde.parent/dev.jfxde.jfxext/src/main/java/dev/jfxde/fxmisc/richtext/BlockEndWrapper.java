package dev.jfxde.fxmisc.richtext;

import org.fxmisc.richtext.GenericStyledArea;

import dev.jfxde.jx.tools.Token;

public class BlockEndWrapper<T extends GenericStyledArea<?, ?, ?>> extends GenericStyledAreaWrapper<T> {

    public BlockEndWrapper(T area) {
        super(area);
    }

    void indentEnd(Token closeToken) {

        int paragraph = getParagraphForAbsolutePosition(closeToken.getStart());
        String paragraphText = getArea().getText(paragraph);

        if (paragraphText.trim().equals(closeToken.getValue())) {
            String indentation = getParagraphIndentation(getParagraphForAbsolutePosition(closeToken.getOppositeToken().getStart()));
            if (!indentation.equals(getParagraphIndentation(paragraph))) {
                int endColumn = getColumnForAbsolutePosition(closeToken.getEnd());
                getArea().replaceText(paragraph, 0, paragraph, endColumn, indentation + closeToken.getValue());
            }
        }
    }
}

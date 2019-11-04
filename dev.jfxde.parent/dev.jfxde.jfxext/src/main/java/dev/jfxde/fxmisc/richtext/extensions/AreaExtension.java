package dev.jfxde.fxmisc.richtext.extensions;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.scene.control.IndexRange;

import static org.fxmisc.richtext.model.TwoDimensional.Bias.Forward;

public abstract class AreaExtension<T extends GenericStyledArea<?, ?, ?>> {

    protected AreaExtensions<? extends GenericStyledArea<?, ?, ?>> areaExtensions;
    protected T area;

    public T getArea() {
        return area;
    }

    void setAreaFeatures(AreaExtensions<? extends GenericStyledArea<?, ?, ?>> areaExtensions) {
        this.areaExtensions = areaExtensions;
    }

    void setArea(T area) {
        this.area = area;
    }

    public void init() {
    }

    boolean isCaretPosition(int position, int insertionEnd) {
        int caretPosition = insertionEnd >= 0 ? insertionEnd : getArea().getCaretPosition();
        boolean isCaretPosition = position == caretPosition || position == caretPosition - 1;
        return isCaretPosition;
    }

    String getCurrentParagraphText() {
        return getArea().getText(getArea().getCurrentParagraph());
    }

    String getCurrentParagraphIndentation() {
        return getParagraphIndentation(getArea().getCurrentParagraph());
    }

    String getParagraphIndentation(int index) {
        String text = getArea().getText(index);
        Matcher matcher = Pattern.compile("( *).*").matcher(text);
        String indentation = matcher.find() ? matcher.group(1) : "";

        return indentation;
    }

    boolean isCaretInIndentation() {
        return getCurrentParagraphText().substring(0, getArea().getCaretColumn()).trim().isEmpty();
    }

    int getParagraphForAbsolutePosition(int position) {
        return getArea().offsetToPosition(position, Forward).getMajor();
    }

    void changeParagraphs(IndexRange range, Function<Integer, String> change) {

        int startParagraph = getParagraphForAbsolutePosition(range.getStart());
        int endParagraph = getParagraphForAbsolutePosition(range.getEnd());
        boolean caretAtEnd = getArea().getCaretPosition() == range.getEnd();

        StringBuilder builder = new StringBuilder();

        for (int i = startParagraph; i <= endParagraph; i++) {
            builder.append(change.apply(i));
            if (i < endParagraph) {
                builder.append("\n");
            }
        }

        String oldText = getArea().getText(range);
        String newText = builder.toString();

        if (oldText.equals(newText)) {
            return;
        }

        getArea().replaceText(getArea().getAbsolutePosition(startParagraph, 0),
                getArea().getAbsolutePosition(endParagraph, getArea().getParagraphLength(endParagraph)), newText);

        if (range.getLength() > 0) {
            if (caretAtEnd) {
                getArea().selectRange(startParagraph, 0, endParagraph, getArea().getParagraphLength(endParagraph));
            } else {
                getArea().selectRange(endParagraph, getArea().getParagraphLength(endParagraph), startParagraph, 0);
            }
        }
    }
}

package dev.jfxde.fxmisc.richtext.extensions;

import static org.fxmisc.wellbehaved.event.EventPattern.keyTyped;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.regex.Matcher;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.wellbehaved.event.Nodes;

public class BlockEndExtension<T extends GenericStyledArea<?,?,?>> extends AreaExtension<T> {

    private Lexer lexer;

    @Override
    public void init() {

        LexerExtension lexerExtension = areaExtensions.getExtension(LexerExtension.class);
        lexer = lexerExtension.getLexer();
        Nodes.addInputMap(getArea(), sequence(
                consume(keyTyped().onlyIf(k -> lexer.isClosingDelimiter(k.getCharacter())), e -> insertBlockEnd(e.getCharacter()))
            ));
    }

    void insertBlockEnd(String closingDelimiter) {
        getArea().insertText(getArea().getCaretPosition(), closingDelimiter);

        if (getCurrentParagraphText().trim().equals(closingDelimiter)) {
            String indentation = getBlockBeginningIndentation(closingDelimiter);
            if (!indentation.equals(getCurrentParagraphIndentation())) {
                getArea().replaceText(getArea().getCurrentParagraph(), 0, getArea().getCurrentParagraph(),
                    getArea().getCaretColumn(), indentation + closingDelimiter);
            }
        }
    }

    private String getBlockBeginningIndentation(String closingDelimiter) {

        String indentation = "";

        int blockBeginningIndex =  getBlockBeginningIndex(closingDelimiter);

        if (blockBeginningIndex >= 0) {
            int paragraphIndex = getParagraphForAbsolutePosition(blockBeginningIndex);

            if (paragraphIndex >= 0) {

               indentation = getParagraphIndentation(paragraphIndex);
            }
        }

        return indentation;
    }

    private int getBlockBeginningIndex(String closingDelimiter) {
        Deque<Integer> openingDelimiterPositions = new ArrayDeque<>();
        Matcher matcher = lexer.getPattern().matcher(getArea().getText());
        String openingDelimiter = lexer.getOpeningDelimiter(closingDelimiter);
        int index = -1;

        while (matcher.find() && matcher.start() <= getArea().getCaretPosition()) {

            String foundToken = matcher.group();
            if (openingDelimiter.equals(foundToken)) {
                openingDelimiterPositions.push(matcher.start());
            } else if (closingDelimiter.equals(foundToken)) {
                if (!openingDelimiterPositions.isEmpty()) {
                    Integer openingDelimiterPosition = openingDelimiterPositions.pop();

                    if (matcher.start() == getArea().getCaretPosition() - 1) {
                        index = openingDelimiterPosition;
                    }
                }
            }
        }

        return index;
    }
}

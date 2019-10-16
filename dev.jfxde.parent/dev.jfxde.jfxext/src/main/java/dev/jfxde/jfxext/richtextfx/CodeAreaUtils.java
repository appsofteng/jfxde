package dev.jfxde.jfxext.richtextfx;

import java.util.function.Consumer;

import org.fxmisc.richtext.CodeArea;

public final class CodeAreaUtils {

    private CodeAreaUtils() {
    }

    public static String getCodeWord(CodeArea codeArea) {

        String code = codeArea.getText();
        int position = codeArea.getCaretPosition();

        StringBuilder word = new StringBuilder();

        getCodeWordPart(code, position - 1, -1, c -> word.insert(0, c));
        getCodeWordPart(code, position, 1, c -> word.append(c));

        return word.toString();
    }

    public static String getCodePrefix(CodeArea codeArea) {

        String code = codeArea.getText();
        int position = codeArea.getCaretPosition();

        StringBuilder word = new StringBuilder();

        getCodeWordPart(code, position - 1, -1, c -> word.insert(0, c));

        return word.toString();
    }

    private static void getCodeWordPart(String code, int position, int step, Consumer<Character> word) {
        int i = position;

        while (i >= 0 && i < code.length()) {
            char c = code.charAt(i);
            if (Character.isJavaIdentifierPart(c)) {
                word.accept(c);
            } else {
                break;
            }

            i += step;
        }
    }
}

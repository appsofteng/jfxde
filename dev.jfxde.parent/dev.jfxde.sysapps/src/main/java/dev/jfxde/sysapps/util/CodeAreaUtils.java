package dev.jfxde.sysapps.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.logic.data.ConsoleOutput;
import javafx.application.Platform;

public final class CodeAreaUtils {

    private CodeAreaUtils() {
    }

    public static void addOutputLater(CodeArea codeArea, String output) {
        Platform.runLater(() -> {
            CodeAreaUtils.addOutput(codeArea, output);
        });
    }

    public static void addOutput(CodeArea codeArea, String output) {
        codeArea.appendText(output);
        codeArea.moveTo(codeArea.getLength());
        codeArea.requestFollowCaret();
    }

    public static void addOutputLater(CodeArea codeArea, List<? extends ConsoleOutput> outputs) {

        if (!outputs.isEmpty()) {
            Platform.runLater(() -> {
                CodeAreaUtils.addOutput(codeArea, outputs);
            });
        }
    }

    public static void addOutput(CodeArea codeArea, List<? extends ConsoleOutput> outputs) {

        if (outputs.isEmpty()) {
            return;
        }

        String text = "";
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        for (ConsoleOutput co : outputs) {
            text += co.getText();
            spansBuilder.add(Collections.singleton("jd-console-" + co.getType().name().toLowerCase()),
                    co.getText().length());
        }

        int from = codeArea.getLength();

        codeArea.appendText(text);

        StyleSpans<Collection<String>> styleSpans = spansBuilder.create();

        codeArea.setStyleSpans(from, styleSpans);
        codeArea.moveTo(codeArea.getLength());
        codeArea.requestFollowCaret();
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

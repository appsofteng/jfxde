package dev.jfxde.sysapps.xjshell;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends CompletionItem {

    private final CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;

    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion,  int[] anchor) {
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
    }

    @Override
    public void complete() {
        codeArea.replaceText(anchor[0], codeArea.getCaretPosition(), suggestion.continuation());
    }

    @Override
    public String toString() {
        return suggestion.continuation();
    }
}

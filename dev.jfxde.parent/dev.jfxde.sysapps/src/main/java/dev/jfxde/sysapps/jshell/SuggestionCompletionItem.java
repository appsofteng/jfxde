package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;
    private final String docCode;
    private String signature = "";
    private String label = "";

    public SuggestionCompletionItem(CodeArea codeArea, String code, Suggestion suggestion, int[] anchor) {
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.docCode = isMethod() ? code.substring(0, anchor[0]) + suggestion.continuation() : suggestion.continuation();
        setLabel();
    }

    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion, int[] anchor, String docCode, String signature) {
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.docCode = docCode;
        this.signature = signature;
        setLabel();
    }

    private void setLabel() {
        label = isMethod() ? suggestion.continuation().substring(0, suggestion.continuation().indexOf("(")) : suggestion.continuation();
        label = signature.isEmpty() ? label : label + " - " + signature;
    }

    public String getDocCode() {
        return docCode;
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public int[] getAnchor() {
        return anchor;
    }

    @Override
    public void complete() {
        codeArea.replaceText(anchor[0], codeArea.getCaretPosition(), suggestion.continuation());
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuggestionCompletionItem
                && ((SuggestionCompletionItem) obj).suggestion.continuation().equals(suggestion.continuation());
    }

    @Override
    public int hashCode() {
        return suggestion.continuation().hashCode();
    }

    @Override
    public String toString() {

        return label;
    }

    private boolean isMethod() {
        return suggestion.continuation().contains("(");
    }
}

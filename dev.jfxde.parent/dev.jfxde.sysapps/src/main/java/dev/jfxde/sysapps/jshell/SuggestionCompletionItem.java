package dev.jfxde.sysapps.jshell;

import java.util.Map;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends JShellCompletionItem {

    private CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;
    private String label = "";

    public SuggestionCompletionItem(JShell jshell, Map<String, String> bundle, CodeArea codeArea, String code, Suggestion suggestion, int[] anchor) {
        super(jshell, bundle, "", "");
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.docCode = isMethod()
                ? code.substring(0, anchor[0]) + suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(") + 1)
                : suggestion.continuation();
        setLabel();
    }

    public SuggestionCompletionItem(JShell jshell, Map<String, String> bundle, CodeArea codeArea, Suggestion suggestion, int[] anchor, String docCode,
            String signature) {
        super(jshell, bundle, docCode, signature);
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    private void setLabel() {
        label = isMethod() ? suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(")) : suggestion.continuation();
        label = signature.isEmpty() ? label : label + " - " + signature;
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
                && ((SuggestionCompletionItem) obj).suggestion.continuation().equals(suggestion.continuation())
                && ((SuggestionCompletionItem) obj).signature.equals(signature);
    }

    @Override
    public int hashCode() {
        return (suggestion.continuation() + signature).hashCode();
    }

    @Override
    public String toString() {

        return label;
    }

    private boolean isMethod() {
        return suggestion.continuation().contains("(");
    }
}

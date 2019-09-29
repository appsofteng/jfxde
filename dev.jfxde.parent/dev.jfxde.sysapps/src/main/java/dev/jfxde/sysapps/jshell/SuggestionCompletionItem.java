package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import dev.jfxde.jfxext.control.editor.DocRef;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;
    private String label = "";

    public SuggestionCompletionItem(CodeArea codeArea, String code, Suggestion suggestion, int[] anchor) {
        super(new DocRef(code.substring(0, anchor[0]) + suggestion.continuation(), ""));
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion, int[] anchor, String docCode,
            String signature) {
        super(new DocRef(docCode, signature));
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    private void setLabel() {
        label = isMethod() ? suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(")) : suggestion.continuation();
        label = getDocRef().getSignature().isEmpty() ? label : label + " - " + getDocRef().getSignature();
    }

    public Suggestion getSuggestion() {
        return suggestion;
    }

    public int[] getAnchor() {
        return anchor;
    }

    @Override
    public void complete() {
        String completion = suggestion.continuation() + (isMethod() && getDocRef().getSignature().endsWith("()") ? ")" : "");
        codeArea.replaceText(anchor[0], codeArea.getCaretPosition(), completion);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof SuggestionCompletionItem
                && ((SuggestionCompletionItem) obj).suggestion.continuation().equals(suggestion.continuation())
                && ((SuggestionCompletionItem) obj).getDocRef().getSignature().equals(getDocRef().getSignature());
    }

    @Override
    public int hashCode() {
        return (suggestion.continuation() + getDocRef().getSignature()).hashCode();
    }

    @Override
    public String toString() {

        return label;
    }

    private boolean isMethod() {
        return suggestion.continuation().contains("(");
    }
}

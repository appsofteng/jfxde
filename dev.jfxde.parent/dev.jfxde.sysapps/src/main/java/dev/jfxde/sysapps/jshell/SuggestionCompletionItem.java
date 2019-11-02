package dev.jfxde.sysapps.jshell;

import java.util.function.Function;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.fxmisc.richtext.features.CompletionItem;
import dev.jfxde.fxmisc.richtext.features.DocRef;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;
    private String label = "";

    public SuggestionCompletionItem(CodeArea codeArea, String code, Suggestion suggestion, int[] anchor) {
        super(new DocRef(getDocCode(code, suggestion, anchor), ""));
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    public SuggestionCompletionItem(CodeArea codeArea, Suggestion suggestion, int[] anchor, String docCode,
            String signature, Function<DocRef, String> documentation) {
        super(new DocRef(docCode, signature, documentation));
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        setLabel();
    }

    private static String getDocCode(String code, Suggestion suggestion, int[] anchor) {
        int i = suggestion.continuation().lastIndexOf("(");
        String docCode = null;
        if (i > 0) {
           docCode = code.substring(0, anchor[0]) + suggestion.continuation().substring(0, i + 1);
        } else {
            docCode = code.substring(0, anchor[0]) + suggestion.continuation();
        }

        return docCode;
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
        String completion = suggestion.continuation();

        if (isMethod() && getDocRef().getSignature().endsWith("()") && completion.endsWith("(")) {
            completion += ")";
        }

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

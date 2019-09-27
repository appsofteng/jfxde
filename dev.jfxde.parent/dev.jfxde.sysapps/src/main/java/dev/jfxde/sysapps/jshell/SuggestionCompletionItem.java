package dev.jfxde.sysapps.jshell;

import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class SuggestionCompletionItem extends CompletionItem {

    private JShell jshell;
    private CodeArea codeArea;
    private final Suggestion suggestion;
    private final int[] anchor;
    private final String docCode;
    private String signature = "";
    private String label = "";

    public SuggestionCompletionItem(JShell jshell, CodeArea codeArea, String code, Suggestion suggestion, int[] anchor) {
        this.jshell = jshell;
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.docCode = isMethod() ? code.substring(0, anchor[0]) + suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(") + 1) : suggestion.continuation();
        setLabel();
    }

    public SuggestionCompletionItem(JShell jshell, CodeArea codeArea, Suggestion suggestion, int[] anchor, String docCode, String signature) {
        this.jshell = jshell;
        this.codeArea = codeArea;
        this.suggestion = suggestion;
        this.anchor = anchor;
        this.docCode = docCode;
        this.signature = signature;
        setLabel();
    }

    private void setLabel() {
        label = isMethod() ? suggestion.continuation().substring(0, suggestion.continuation().lastIndexOf("(")) : suggestion.continuation();
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

    @Override
    protected String loadDocumentation() {
        List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(getDocCode(), getDocCode().length(), true);

        String documentation = "<strong>" + signature + "</strong><br><br>" + docs.stream().filter(d -> d.signature().equals(signature)).findFirst().map(Documentation::javadoc).orElse("");

        return documentation;
    }
}

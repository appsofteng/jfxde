package dev.jfxde.sysapps.jshell;

import java.util.Map;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class QualifiedNameCompletionItem extends JShellCompletionItem {

    private final ObservableList<TextStyleSpans> input;

    public QualifiedNameCompletionItem(JShell jshell, Map<String,String> bundle, ObservableList<TextStyleSpans> input, String signature) {
        super(jshell, bundle, signature, signature);
        this.input = input;
    }

    @Override
    public void complete() {
        input.add(new TextStyleSpans(String.format("import %s;%n", signature)));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QualifiedNameCompletionItem && ((QualifiedNameCompletionItem)obj).signature.equals(signature);
    }

    @Override
    public int hashCode() {
        return signature.hashCode();
    }

    @Override
    public String toString() {
        return signature;
    }
}

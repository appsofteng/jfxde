package dev.jfxde.sysapps.jshell;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ObservableList;

public class QualifiedNameCompletionItem extends CompletionItem {

    private final ObservableList<TextStyleSpans> input;
    private final String name;

    public QualifiedNameCompletionItem(ObservableList<TextStyleSpans> input, String name) {
        this.input = input;
        this.name = name;
    }

    @Override
    public void complete() {
        input.add(new TextStyleSpans(String.format("import %s;%n", name)));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QualifiedNameCompletionItem && ((QualifiedNameCompletionItem)obj).name.equals(name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }
}

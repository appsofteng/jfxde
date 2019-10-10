package dev.jfxde.sysapps.jshell;

import java.util.function.Function;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.jfxext.richtextfx.features.CompletionItem;
import dev.jfxde.jfxext.richtextfx.features.DocRef;
import javafx.collections.ObservableList;

public class QualifiedNameCompletionItem extends CompletionItem {

    private final ObservableList<TextStyleSpans> input;

    public QualifiedNameCompletionItem(ObservableList<TextStyleSpans> input, String signature, Function<DocRef, String> documentation) {
        super(new DocRef(signature, signature, documentation));
        this.input = input;
    }

    @Override
    public void complete() {
        input.add(new TextStyleSpans(String.format("import %s;\n", getDocRef().getSignature())));
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof QualifiedNameCompletionItem
                && ((QualifiedNameCompletionItem) obj).getDocRef().getSignature().equals(getDocRef().getSignature());
    }

    @Override
    public int hashCode() {
        return getDocRef().getSignature().hashCode();
    }

    @Override
    public String toString() {
        return getDocRef().getSignature();
    }
}

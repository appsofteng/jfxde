package dev.jfxde.sysapps.jshell;

import java.util.function.Consumer;
import java.util.function.Function;

import dev.jfxde.fxmisc.richtext.CompletionItem;
import dev.jfxde.fxmisc.richtext.DocRef;

public class QualifiedNameCompletionItem extends CompletionItem {

    private final Consumer<String> input;

    public QualifiedNameCompletionItem(Consumer<String> input, String signature, Function<DocRef, String> documentation) {
        super(new DocRef(signature, signature, documentation));
        this.input = input;
    }

    @Override
    public void complete() {
        input.accept(String.format("import %s;\n", getDocRef().getSignature()));
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

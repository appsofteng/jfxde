package dev.jfxde.fxmisc.richtext.features;

public abstract class CompletionItem implements Comparable<CompletionItem> {

    private DocRef docRef;

    public CompletionItem(DocRef docRef) {
        this.docRef = docRef;
    }

    public DocRef getDocRef() {

        return docRef;
    }

    public abstract void complete();


    @Override
    public int compareTo(CompletionItem o) {
        return toString().compareTo(o.toString());
    }
}

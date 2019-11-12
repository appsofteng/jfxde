package dev.jfxde.sysapps.jshell;

import java.util.function.Function;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.fxmisc.richtext.CompletionItem;
import dev.jfxde.fxmisc.richtext.DocRef;

public class CommandCompletionItem extends CompletionItem {

    private CodeArea codeArea;
    private int anchor;
    private String continuation;
    private String name;

    public CommandCompletionItem(CodeArea codeArea, int anchor, String continuation, String name, String docCode, Function<DocRef, String> documentation) {
        super(new DocRef(docCode, name, documentation));
        this.codeArea = codeArea;
        this.anchor = anchor;
        this.continuation = continuation;
        this.name = name;
    }

    @Override
    public void complete() {
        codeArea.insertText(anchor, continuation);
    }

    @Override
    public String toString() {
        return name;
    }
}

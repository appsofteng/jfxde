package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import dev.jfxde.jfxext.control.editor.DocRef;

public class CommandCompletionItem extends CompletionItem {

    private int anchor;
    private String continuation;
    private String name;
    private CodeArea codeArea;

    public CommandCompletionItem(CodeArea codeArea, int anchor, String continuation, String name) {
        super(new DocRef(""));
        this.anchor = anchor;
        this.continuation = continuation;
        this.name = name;
        this.codeArea = codeArea;
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

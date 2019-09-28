package dev.jfxde.sysapps.jshell;

import java.util.Map;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import jdk.jshell.JShell;

public abstract  class JShellCompletionItem extends CompletionItem {

    private JShell jshell;
    private Map<String,String> bundle;
    protected String docCode = "";
    protected String signature = "";

    public JShellCompletionItem(JShell jshell, Map<String,String> bundle, String docCode, String signature) {
        this.jshell = jshell;
        this.bundle = bundle;
        this.docCode = docCode;
        this.signature = signature;
    }

    @Override
    protected String loadDocumentation() {
        String documentation = JShellUtils.getDocumentation(jshell, docCode, signature, bundle);

        return documentation;
    }

    public String getDocCode() {
        return docCode;
    }
}

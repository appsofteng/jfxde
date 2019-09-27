package dev.jfxde.sysapps.jshell;

import java.util.List;
import java.util.Map;

import dev.jfxde.jfxext.control.editor.CompletionItem;
import dev.jfxde.jfxext.util.JavadocUtils;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.Documentation;

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
        List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(docCode, docCode.length(), true);

        String javadoc = docs.stream().filter(d -> d.signature().equals(signature)).findFirst().map(Documentation::javadoc).orElse("");
        String documentation = "<strong><code>" + signature + "</code></strong><br><br>" + JavadocUtils.toHtml(javadoc, bundle);

        return documentation;
    }

    public String getDocCode() {
        return docCode;
    }
}

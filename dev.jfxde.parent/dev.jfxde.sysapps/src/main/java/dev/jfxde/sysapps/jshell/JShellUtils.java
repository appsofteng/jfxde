package dev.jfxde.sysapps.jshell;

import java.util.List;
import java.util.Map;

import dev.jfxde.jfxext.util.JavadocUtils;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.SourceCodeAnalysis.Documentation;

public final class JShellUtils {

    private JShellUtils() {
    }

    public static Snippet getSnippet(JShell jshell, Integer id) {
        Snippet snippet = jshell.snippets().filter(s -> s.id().equals(id.toString())).findFirst().orElse(null);

        return snippet;
    }

    public static String getDocumentation(JShell jshell, String docCode, String signature, Map<String,String> bundle) {
        List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(docCode, docCode.length(), true);

        String javadoc = docs.stream().filter(d -> d.signature().equals(signature)).findFirst().map(Documentation::javadoc).orElse("");
        String documentation = javadoc.isEmpty() ? "" : "<strong><code>" + signature + "</code></strong><br><br>" + JavadocUtils.toHtml(javadoc, bundle);

        return documentation;
    }

    public static String getDocumentation(JShell jshell, String docCode, Map<String,String> bundle) {
        List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(docCode, docCode.length(), true);
        Documentation documentation = docs.stream().filter(d -> d.signature().matches("[\\w\\.]*" + docCode + "[\\w<>(), ]*")).findFirst().orElse(null);
        String result = documentation == null ? "" : "<strong><code>" + documentation.signature() + "</code></strong><br><br>" + JavadocUtils.toHtml(documentation.javadoc(), bundle);

        return result;
    }
}

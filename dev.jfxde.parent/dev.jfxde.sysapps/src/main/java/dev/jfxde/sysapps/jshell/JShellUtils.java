package dev.jfxde.sysapps.jshell;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

import dev.jfxde.fxmisc.richtext.DocRef;
import dev.jfxde.jx.tools.JavadocUtils;
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

    public static void loadSnippets(JShell jshell, InputStream in) throws IOException {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            reader.lines().forEach(s -> jshell.eval(s));
        }
    }

    public static String getDocumentation(JShell jshell, DocRef docRef, Map<String, String> bundle) {

        List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(docRef.getDocCode(), docRef.getDocCode().length(), true);

        Documentation documentation = docs.stream().filter(d -> matches(d.signature(), docRef)).findFirst().orElse(null);

        String result = documentation == null ? ""
                : "<strong><code>" + documentation.signature() + "</code></strong><br><br>" + JavadocUtils.toHtml(documentation.javadoc(), bundle);

        return result;
    }

    private static boolean matches(String signature, DocRef docRef) {

        return docRef.getSignature() != null && !docRef.getSignature().isEmpty() ? docRef.getSignature().equals(signature)
                : signature.matches("[\\w\\.]*" + docRef.getDocCode() + "[\\w<>(), ]*");
    }
}

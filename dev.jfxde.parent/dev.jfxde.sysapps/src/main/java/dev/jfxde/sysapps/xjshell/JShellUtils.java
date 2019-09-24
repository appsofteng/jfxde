package dev.jfxde.sysapps.xjshell;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public final class JShellUtils {

    private JShellUtils() {
    }

    public static Snippet getSnippet(JShell jshell, Integer id) {
        Snippet snippet = jshell.snippets().filter(s -> s.id().equals(id.toString())).findFirst().orElse(null);

        return snippet;
    }
}

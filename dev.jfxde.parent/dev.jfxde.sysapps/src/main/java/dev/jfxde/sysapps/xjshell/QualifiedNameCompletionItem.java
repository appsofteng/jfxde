package dev.jfxde.sysapps.xjshell;

import jdk.jshell.JShell;

public class QualifiedNameCompletionItem extends CompletionItem {

    private final JShell jshell;
    private final String name;

    public QualifiedNameCompletionItem(JShell jshell, String name) {
        this.jshell = jshell;
        this.name = name;
    }

    @Override
    public void complete() {
        jshell.eval(String.format("import %s;", name));
    }

    @Override
    public String toString() {
        return name;
    }
}

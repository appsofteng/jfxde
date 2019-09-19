package dev.jfxde.sysapps.jshell.commands;

import java.util.Arrays;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.JShell;

public abstract class Command {

    private final String name;
    protected final JShell jshell;
    protected final CodeArea outputArea;

    public Command(String name, JShell jshell, CodeArea outputArea) {
        this.name = name;
        this.jshell = jshell;
        this.outputArea = outputArea;
    }

    public boolean matches(String input) {
        return input.startsWith(name);
    }

    public String getName() {
        return name;
    }

    public void execute(String input) {
        String[] parts = input.split(" +");
        parts = Arrays.copyOfRange(parts, 1, parts.length);
        execute(new SnippetMatch(parts));
    }

    protected abstract void execute(SnippetMatch input);

    @Override
    public String toString() {
        return name;
    }

}

package dev.jfxde.sysapps.jshell.commands;

import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.jshell.SnippetOutput;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class RerunCommand extends Command {

    private final SnippetOutput snippetOutput;
    private SnippetMatch snippetMatch;

    public RerunCommand(JShell jshell, CodeArea outputArea, SnippetOutput snippetOutput, SnippetMatch snippetMatch) {
        super("/{id|startID-endID}[ {id|startID-endID|name}...]", jshell, outputArea);
        this.snippetOutput = snippetOutput;
        this.snippetMatch = snippetMatch;
    }

    @Override
    public boolean matches(String input) {
        return input.matches("/(\\d+|\\d+-\\d+)( (\\d+|\\d+-\\d+|\\w+))*");
    }

    @Override
    public void execute(String input) {

        String[] parts = input.substring(1).split(" +");

        List<Snippet> snippets = snippetMatch.matches(parts);

        snippetOutput.output(snippets);
    }
}

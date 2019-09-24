package dev.jfxde.sysapps.xjshell.commands;

import java.util.List;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.xjshell.SnippetOutput;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class RerunCommand extends Command {

    private final SnippetOutput snippetOutput;
    private SnippetMatch snippetMatch;

    public RerunCommand(JShell jshell, ObservableList<TextStyleSpans> output, SnippetOutput snippetOutput, SnippetMatch snippetMatch) {
        super("/{id|startID-endID}[ {id|startID-endID|name}...]", jshell, output);
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

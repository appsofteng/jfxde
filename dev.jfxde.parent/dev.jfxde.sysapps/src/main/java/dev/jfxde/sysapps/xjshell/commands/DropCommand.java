package dev.jfxde.sysapps.xjshell.commands;

import java.util.Arrays;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class DropCommand extends Command {

    private SnippetMatch snippetMatch;

    public DropCommand(JShell jshell, ObservableList<TextStyleSpans> output, SnippetMatch snippetMatch) {
        super("/drop", jshell, output);
        this.snippetMatch = snippetMatch;
    }

    @Override
    public void execute(String input) {

        String[] parts = input.split(" +");
        parts = Arrays.copyOfRange(parts, 1, parts.length);
        List<Snippet> snippets = snippetMatch.matches(parts);

        StringBuilder sb = new StringBuilder();
        snippets.forEach(s -> {
                    sb.append("dropped" + SnippetUtils.toString(s, jshell));
                    jshell.drop(s);
                });

        output.add(new TextStyleSpans(sb.toString() + "\n", ConsoleModel.COMMENT_STYLE));
    }
}

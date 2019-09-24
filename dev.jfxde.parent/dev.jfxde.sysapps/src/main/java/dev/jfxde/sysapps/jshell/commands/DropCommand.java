package dev.jfxde.sysapps.jshell.commands;

import java.util.Arrays;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class DropCommand extends Command {

    private SnippetMatch snippetMatch;

    public DropCommand(JShell jshell, ConsoleModel consoleModel, SnippetMatch snippetMatch) {
        super("/drop", jshell, consoleModel);
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

        consoleModel.getOutput().add(new TextStyleSpans(sb.toString() + "\n", ConsoleModel.COMMENT_STYLE));
    }
}

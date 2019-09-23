package dev.jfxde.sysapps.xjshell.commands;

import java.util.Arrays;
import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class DropCommand extends Command {

    private SnippetMatch snippetMatch;

    public DropCommand(JShell jshell, CodeArea outputArea, SnippetMatch snippetMatch) {
        super("/drop", jshell, outputArea);
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

        CodeAreaUtils.addOutputLater(outputArea, new ConsoleOutput(sb.toString() + "\n", Type.COMMENT));
    }
}

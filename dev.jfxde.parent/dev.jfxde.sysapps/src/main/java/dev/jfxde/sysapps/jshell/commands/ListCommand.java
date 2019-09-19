package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.jshell.SnippetUtils;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class ListCommand extends Command {

    public ListCommand(JShell jshell, CodeArea outputArea) {
        super("/list", jshell, outputArea);
    }

    @Override
    public void execute(SnippetMatch input) {
        String output = jshell.snippets().filter(s -> jshell.status(s).isActive()).map(s -> SnippetUtils.toString(s, jshell))
                .collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

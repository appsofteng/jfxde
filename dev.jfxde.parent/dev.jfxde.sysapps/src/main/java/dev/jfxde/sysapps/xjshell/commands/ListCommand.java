package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.util.CodeAreaUtils;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import jdk.jshell.JShell;

public class ListCommand extends Command {

    public ListCommand(JShell jshell, CodeArea outputArea) {
        super("/list", jshell, outputArea);
    }

    @Override
    public void execute(String input) {
        String output = jshell.snippets().filter(s -> jshell.status(s).isActive()).map(s -> SnippetUtils.toString(s, jshell))
                .collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.jshell.SnippetUtils;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class MethodCommand extends Command {

    public MethodCommand(JShell jshell, CodeArea outputArea) {
        super("/methods", jshell, outputArea);
    }

    @Override
    public void execute(SnippetMatch input) {
        String output = jshell.methods().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

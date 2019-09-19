package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.jshell.SnippetUtils;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class VarCommand extends Command {

    public VarCommand(JShell jshell, CodeArea outputArea) {
        super("/vars", jshell, outputArea);
    }

    @Override
    public void execute(SnippetMatch input) {
        String output = jshell.variables().map(s -> SnippetUtils.toString(s, jshell.varValue(s))).collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

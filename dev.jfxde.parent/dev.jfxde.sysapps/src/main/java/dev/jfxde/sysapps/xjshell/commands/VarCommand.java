package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.util.CodeAreaUtils;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import jdk.jshell.JShell;

public class VarCommand extends Command {

    public VarCommand(JShell jshell, CodeArea outputArea) {
        super("/vars", jshell, outputArea);
    }

    @Override
    public void execute(String input) {
        String output = jshell.variables().map(s -> SnippetUtils.toString(s, jshell.varValue(s))).collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

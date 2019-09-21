package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.sysapps.jshell.SnippetUtils;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class ImportCommand extends Command {

    public ImportCommand(JShell jshell, CodeArea outputArea) {
        super("/imports", jshell, outputArea);

    }

    @Override
    public void execute(String input) {
        String output = jshell.imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        CodeAreaUtils.addOutputLater(outputArea, output);
    }
}

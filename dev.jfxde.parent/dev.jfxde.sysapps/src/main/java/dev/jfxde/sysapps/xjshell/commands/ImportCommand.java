package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class ImportCommand extends Command {

    public ImportCommand(JShell jshell, ObservableList<TextStyleSpans> output) {
        super("/imports", jshell, output);

    }

    @Override
    public void execute(String input) {
        String imports = jshell.imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        output.add(new TextStyleSpans(imports));
    }
}

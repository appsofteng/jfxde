package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class TypeCommand extends Command {

    public TypeCommand(JShell jshell, ObservableList<TextStyleSpans> output) {
        super("/types", jshell, output);
    }

    @Override
    public void execute(String input) {
        String result = jshell.types().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        output.add(new TextStyleSpans(result));
    }
}

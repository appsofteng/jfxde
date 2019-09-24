package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class VarCommand extends Command {

    public VarCommand(JShell jshell, ObservableList<TextStyleSpans> output) {
        super("/vars", jshell, output);
    }

    @Override
    public void execute(String input) {
        String vars = jshell.variables().map(s -> SnippetUtils.toString(s, jshell.varValue(s))).collect(Collectors.joining()) + "\n";

        output.add(new TextStyleSpans(vars));
    }
}

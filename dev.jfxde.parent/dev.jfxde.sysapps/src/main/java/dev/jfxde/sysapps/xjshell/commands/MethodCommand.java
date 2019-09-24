package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class MethodCommand extends Command {

    public MethodCommand(JShell jshell, ObservableList<TextStyleSpans> output) {
        super("/methods", jshell, output);
    }

    @Override
    public void execute(String input) {
        String result = jshell.methods().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        output.add(new TextStyleSpans(result));
    }
}

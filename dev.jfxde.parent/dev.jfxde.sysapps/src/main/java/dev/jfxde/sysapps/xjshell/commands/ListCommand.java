package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.xjshell.SnippetUtils;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class ListCommand extends Command {

    public ListCommand(JShell jshell, ObservableList<TextStyleSpans> output) {
        super("/list", jshell, output);
    }

    @Override
    public void execute(String input) {
        String result = jshell.snippets().filter(s -> jshell.status(s).isActive()).map(s -> SnippetUtils.toString(s, jshell))
                .collect(Collectors.joining()) + "\n";

        output.add(new TextStyleSpans(result));
    }
}

package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;

public class ListCommand extends Command {

    public ListCommand(JShell jshell, ConsoleModel consoleModel) {
        super("/list", jshell, consoleModel);
    }

    @Override
    public void execute(String input) {
        String result = jshell.snippets().filter(s -> jshell.status(s).isActive()).map(s -> SnippetUtils.toString(s, jshell))
                .collect(Collectors.joining()) + "\n";

        consoleModel.getOutput().add(new TextStyleSpans(result));
    }
}

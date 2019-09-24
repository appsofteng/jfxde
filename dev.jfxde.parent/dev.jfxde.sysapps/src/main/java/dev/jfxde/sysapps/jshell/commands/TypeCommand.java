package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;

public class TypeCommand extends Command {

    public TypeCommand(JShell jshell, ConsoleModel consoleModel) {
        super("/types", jshell, consoleModel);
    }

    @Override
    public void execute(String input) {
        String result = jshell.types().map(SnippetUtils::toString).sorted().collect(Collectors.joining()) + "\n";

        consoleModel.getOutput().add(new TextStyleSpans(result));
    }
}

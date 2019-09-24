package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;

public class VarCommand extends Command {

    public VarCommand(JShell jshell, ConsoleModel consoleModel) {
        super("/vars", jshell, consoleModel);
    }

    @Override
    public void execute(String input) {
        String vars = jshell.variables().map(s -> SnippetUtils.toString(s, jshell.varValue(s))).collect(Collectors.joining()) + "\n";

        consoleModel.getOutput().add(new TextStyleSpans(vars));
    }
}

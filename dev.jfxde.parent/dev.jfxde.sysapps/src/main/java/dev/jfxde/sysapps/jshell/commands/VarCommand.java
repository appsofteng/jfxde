package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/vars")
public class VarCommand extends BaseCommand {

    public VarCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {
        String vars = jshell.variables().map(s -> SnippetUtils.toString(s, jshell.varValue(s))).collect(Collectors.joining());

        consoleModel.addNewLineOutput(new TextStyleSpans(vars));
    }
}

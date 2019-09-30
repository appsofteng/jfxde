package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;
import picocli.CommandLine.Command;

@Command(name = "/types")
public class TypeCommand extends BaseCommand {

    public TypeCommand(AppContext context,JShell jshell, ConsoleModel consoleModel) {
        super(context, jshell, consoleModel);
    }

    @Override
    public void run() {
        String result = jshell.types().map(SnippetUtils::toString).sorted().collect(Collectors.joining());

        consoleModel.addNewLineOutput(new TextStyleSpans(result));
    }
}

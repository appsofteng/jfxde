package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.JShell;
import picocli.CommandLine.Command;

@Command(name = "/imports")
public class ImportCommand extends BaseCommand {

    public ImportCommand(AppContext context, JShell jshell, ConsoleModel consoleModel) {
        super(context, jshell, consoleModel);

    }

    @Override
    public void run() {
        String imports = jshell.imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining());

        consoleModel.addNewLineOutput(new TextStyleSpans(imports));
    }
}

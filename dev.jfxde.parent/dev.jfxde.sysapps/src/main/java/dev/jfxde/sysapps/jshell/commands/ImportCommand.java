package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/imports")
public class ImportCommand extends BaseCommand {

    public ImportCommand(CommandOutput commandOutput) {
        super(commandOutput);

    }

    @Override
    public void run() {
        String imports = jshell.imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining());

        consoleModel.addNewLineOutput(new TextStyleSpans(imports));
    }
}

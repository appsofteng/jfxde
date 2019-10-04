package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/imports")
public class ImportCommand extends BaseCommand {

    public ImportCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);

    }

    @Override
    public void run() {
        String imports = commandProcessor.getSession().getJshell().imports().map(SnippetUtils::toString).sorted().collect(Collectors.joining());

        commandProcessor.getSession().getFeedback().normaln(imports);
    }
}

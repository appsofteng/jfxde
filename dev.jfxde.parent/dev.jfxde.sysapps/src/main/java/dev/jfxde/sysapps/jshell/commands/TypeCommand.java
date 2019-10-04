package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/types")
public class TypeCommand extends BaseCommand {

    public TypeCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        String result = commandProcessor.getSession().getJshell().types().map(SnippetUtils::toString).sorted().collect(Collectors.joining());

        commandProcessor.getSession().getFeedback().normaln(result);
    }
}

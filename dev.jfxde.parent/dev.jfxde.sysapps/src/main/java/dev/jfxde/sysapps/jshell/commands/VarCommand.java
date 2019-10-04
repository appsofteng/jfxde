package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/vars")
public class VarCommand extends BaseCommand {

    public VarCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        String vars = commandProcessor.getSession().getJshell().variables().map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell().varValue(s))).collect(Collectors.joining());

        commandProcessor.getSession().getFeedback().normaln(vars);
    }
}

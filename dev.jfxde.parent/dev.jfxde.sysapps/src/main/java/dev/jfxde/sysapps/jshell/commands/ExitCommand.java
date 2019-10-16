package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = ExitCommand.EXIT_COMMAND)
public class ExitCommand extends BaseCommand {

    public static final String EXIT_COMMAND = "/exit";

    public ExitCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().getContext().ac().stop();
    }
}

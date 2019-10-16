package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/help")
public class HelpCommand extends BaseCommand {

    public HelpCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

       commandProcessor.getCommandLine().usage(commandProcessor.getOut());
    }
}

package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandOutput;
import picocli.CommandLine.Command;

@Command(name = "/help")
public class HelpCommand extends BaseCommand {

    public HelpCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {

       commandOutput.getCommandLine().usage(commandOutput.getOut());
    }
}

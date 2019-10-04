package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/reload")
public class ReloadCommand extends BaseCommand {

    public ReloadCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {


    }
}

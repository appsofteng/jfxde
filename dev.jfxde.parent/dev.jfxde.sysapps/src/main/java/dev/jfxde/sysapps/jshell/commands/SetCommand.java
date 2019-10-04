package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/set")
public class SetCommand extends BaseCommand {

    public SetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {


    }
}

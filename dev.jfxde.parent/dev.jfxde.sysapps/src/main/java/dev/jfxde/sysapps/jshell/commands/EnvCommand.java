package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/env")
public class EnvCommand extends BaseCommand {

    public EnvCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {


    }
}

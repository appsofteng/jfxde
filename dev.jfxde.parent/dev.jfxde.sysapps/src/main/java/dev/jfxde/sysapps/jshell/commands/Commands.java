package dev.jfxde.sysapps.jshell.commands;

import picocli.CommandLine.Command;

@Command(name = "", hidden = true, subcommands = { DropCommand.class, EnvCommand.class, HelpCommand.class, HistoryCommand.class, ImportCommand.class, ListCommand.class,
        MethodCommand.class, OpenCommand.class, ReloadCommand.class, RerunCommand.class, ResetCommand.class, SaveCommand.class, SetCommand.class, TypeCommand.class, VarCommand.class })
public class Commands {

}

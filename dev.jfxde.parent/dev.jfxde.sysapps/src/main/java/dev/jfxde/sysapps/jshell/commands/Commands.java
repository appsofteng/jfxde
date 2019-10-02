package dev.jfxde.sysapps.jshell.commands;

import picocli.CommandLine.Command;

@Command(name = "", hidden = true, subcommands = { DropCommand.class, HistoryCommand.class, ImportCommand.class, ListCommand.class,
        MethodCommand.class, OpenCommand.class, RerunCommand.class, SaveCommand.class, TypeCommand.class, VarCommand.class })
public class Commands {

}

package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import picocli.CommandLine.Command;

@Command(name = "/history")
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {
        String result = history.stream().collect(Collectors.joining("\n"));

        consoleModel.addNewLineOutput(new TextStyleSpans(result + "\n", ConsoleModel.COMMENT_STYLE));
    }
}

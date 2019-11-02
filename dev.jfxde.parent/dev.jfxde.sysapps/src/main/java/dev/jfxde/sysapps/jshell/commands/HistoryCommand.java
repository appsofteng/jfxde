package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfx.scene.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/history")
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {
        String result = commandProcessor.getSession().getHistory().stream().collect(Collectors.joining("\n"));

        commandProcessor.getSession().getFeedback().normaln(result + "\n", ConsoleModel.COMMENT_STYLE);
    }
}

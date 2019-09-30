package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;
import picocli.CommandLine.Command;

@Command(name = "/history")
public class HistoryCommand extends BaseCommand {

    public HistoryCommand(AppContext context, JShell jshell, ConsoleModel consoleModel, ObservableList<String> history) {
        super(context, jshell, consoleModel, history);
    }

    @Override
    public void run() {
        String result = history.stream().collect(Collectors.joining("\n"));

        consoleModel.addNewLineOutput(new TextStyleSpans(result + "\n", ConsoleModel.COMMENT_STYLE));
    }
}

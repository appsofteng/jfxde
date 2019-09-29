package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class HistoryCommand extends Command {

    public HistoryCommand(JShell jshell, ConsoleModel consoleModel, ObservableList<String> history) {
        super("/history", jshell, consoleModel, history);
    }

    @Override
    public void execute(String input) {
        String result = history.stream().collect(Collectors.joining("\n"));

        consoleModel.getOutput().add(new TextStyleSpans(result + "\n\n", ConsoleModel.COMMENT_STYLE));
    }
}

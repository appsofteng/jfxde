package dev.jfxde.sysapps.xjshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class HistoryCommand extends Command {

    public HistoryCommand(JShell jshell, ObservableList<TextStyleSpans> output, ObservableList<TextStyleSpans> history) {
        super("/history", jshell, output, history);
    }

    @Override
    public void execute(String input) {
        String history = output.stream().map(TextStyleSpans::getText).collect(Collectors.joining("\n"));

        output.add(new TextStyleSpans(history + "\n\n", ConsoleModel.COMMENT_STYLE));
    }
}

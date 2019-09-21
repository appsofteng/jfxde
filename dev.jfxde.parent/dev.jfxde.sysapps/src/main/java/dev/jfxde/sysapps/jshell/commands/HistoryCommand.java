package dev.jfxde.sysapps.jshell.commands;

import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class HistoryCommand extends Command {

    public HistoryCommand(JShell jshell, CodeArea outputArea, List<String> history) {
        super("/history", jshell, outputArea, history);
    }

    @Override
    public void execute(String input) {
        String output = history.stream().collect(Collectors.joining("\n"));

        CodeAreaUtils.addOutputLater(outputArea, new ConsoleOutput(output + "\n\n", Type.COMMENT));
    }
}

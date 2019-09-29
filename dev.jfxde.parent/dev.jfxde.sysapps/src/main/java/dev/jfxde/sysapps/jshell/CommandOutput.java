package dev.jfxde.sysapps.jshell;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.commands.Command;
import dev.jfxde.sysapps.jshell.commands.DropCommand;
import dev.jfxde.sysapps.jshell.commands.HistoryCommand;
import dev.jfxde.sysapps.jshell.commands.ImportCommand;
import dev.jfxde.sysapps.jshell.commands.ListCommand;
import dev.jfxde.sysapps.jshell.commands.MethodCommand;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import dev.jfxde.sysapps.jshell.commands.SnippetMatch;
import dev.jfxde.sysapps.jshell.commands.TypeCommand;
import dev.jfxde.sysapps.jshell.commands.VarCommand;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public class CommandOutput extends JShellOutput {

    private final List<Command> commands;

    CommandOutput(AppContext context, JShell jshell, ConsoleModel consoleModel, ObservableList<String> history, SnippetOutput snippetOutput) {
        super(context, jshell, consoleModel, history);

        SnippetMatch snippetMatch = new SnippetMatch(jshell);
        commands = List.of(new DropCommand(jshell, consoleModel, snippetMatch), new HistoryCommand(jshell, consoleModel, history), new ImportCommand(jshell, consoleModel),
                new ListCommand(jshell, consoleModel), new MethodCommand(jshell, consoleModel),
                new RerunCommand(jshell, consoleModel, snippetOutput, snippetMatch), new TypeCommand(jshell, consoleModel), new VarCommand(jshell, consoleModel));
    }

    @Override
    void output(String input) {

        List<Command> matchingCommands = commands.stream().filter(c -> c.matches(input)).collect(Collectors.toList());

        if (matchingCommands.isEmpty()) {
            matchingCommands = commands;
        }

        if (matchingCommands.size() > 1) {
            consoleModel.getOutput().add(new TextStyleSpans(String.format("Possible commands: %s%n%n", matchingCommands)));
        } else {
            matchingCommands.get(0).execute(input);
        }
    }
}

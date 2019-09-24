package dev.jfxde.sysapps.jshell;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
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

    CommandOutput(AppContext context, JShell jshell, ObservableList<TextStyleSpans> output, ObservableList<TextStyleSpans> history, SnippetOutput snippetOutput) {
        super(context, jshell, output, history);

        SnippetMatch snippetMatch = new SnippetMatch(jshell);
        commands = List.of(new DropCommand(jshell, output, snippetMatch), new HistoryCommand(jshell, output, history), new ImportCommand(jshell, output),
                new ListCommand(jshell, output), new MethodCommand(jshell, output),
                new RerunCommand(jshell, output, snippetOutput, snippetMatch), new TypeCommand(jshell, output), new VarCommand(jshell, output));
    }

    @Override
    void output(String input) {

        List<Command> matchingCommands = commands.stream().filter(c -> c.matches(input)).collect(Collectors.toList());

        if (matchingCommands.isEmpty()) {
            matchingCommands = commands;
        }

        if (matchingCommands.size() > 1) {
            output.add(new TextStyleSpans(String.format("Possible commands: %s%n%n", matchingCommands)));
        } else {
            matchingCommands.get(0).execute(input);
        }
    }
}

package dev.jfxde.sysapps.jshell;

import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
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
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class CommandOutput extends JShellOutput {

    private final List<Command> commands;

    CommandOutput(AppContext context, JShell jshell, CodeArea outputArea, SnippetOutput snippetOutput, List<String> history) {
        super(context, jshell, outputArea, history);

        SnippetMatch snippetMatch = new SnippetMatch(jshell);
        commands = List.of(new DropCommand(jshell, outputArea, snippetMatch), new HistoryCommand(jshell, outputArea, history), new ImportCommand(jshell, outputArea), new ListCommand(jshell, outputArea), new MethodCommand(jshell, outputArea),
                new RerunCommand(jshell, outputArea, snippetOutput, snippetMatch), new TypeCommand(jshell, outputArea), new VarCommand(jshell, outputArea));
    }

    @Override
    void output(String input) {

        List<Command> matchingCommands = commands.stream().filter(c -> c.matches(input)).collect(Collectors.toList());

        if (matchingCommands.isEmpty()) {
            matchingCommands = commands;
        }

        if (matchingCommands.size() > 1) {
            CodeAreaUtils.addOutputLater(outputArea, String.format("Possible commands: %s%n%n", matchingCommands));
        } else {
            matchingCommands.get(0).execute(input);
        }
    }
}

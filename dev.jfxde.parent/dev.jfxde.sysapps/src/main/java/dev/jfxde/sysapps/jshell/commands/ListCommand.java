package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;

@Command(name = "/list")
public class ListCommand extends BaseCommand {

    public ListCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {
        String result = jshell.snippets().filter(s -> jshell.status(s).isActive()).map(s -> SnippetUtils.toString(s, jshell))
                .collect(Collectors.joining());

        consoleModel.addNewLineOutput(new TextStyleSpans(result));
    }
}

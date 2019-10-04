package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/list")
public class ListCommand extends BaseCommand {

    public ListCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Option(names = "-all", descriptionKey = "/list.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/list.-start")
    private boolean start;

    @Override
    public void run() {

        if (all && start) {
            commandOutput.getCommandLine().getErr().println(context.rc().getString("onlyOneOptionAllowed") + "\n");
            return;
        }

        String result = "";
        if (all) {
            result = jshell.snippets()
                    .map(s -> SnippetUtils.toString(s, jshell))
                    .collect(Collectors.joining());
        } else if (start) {
            result = jshell.snippets()
                    .filter(s -> Integer.parseInt(s.id()) <= jshellContent.startSnippetMaxIndex)
                    .map(s -> SnippetUtils.toString(s, jshell))
                    .collect(Collectors.joining());
        } else {

            result = jshell.snippets()
                    .filter(s -> jshell.status(s).isActive())
                    .filter(s -> Integer.parseInt(s.id()) > jshellContent.startSnippetMaxIndex)
                    .map(s -> SnippetUtils.toString(s, jshell))
                    .collect(Collectors.joining());
        }

        consoleModel.addNewLineOutput(new TextStyleSpans(result));
    }
}

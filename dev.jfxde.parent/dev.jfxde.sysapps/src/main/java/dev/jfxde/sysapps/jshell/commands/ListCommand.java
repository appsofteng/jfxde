package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/list")
public class ListCommand extends BaseCommand {

    public ListCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Option(names = "-all", descriptionKey = "/list.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/list.-start")
    private boolean start;

    @Override
    public void run() {

        if (all && start) {
            commandProcessor.getCommandLine().getErr()
                    .println(commandProcessor.getSession().getContext().rc().getString("onlyOneOptionAllowed") + "\n");
            return;
        }

        String result = "";
        if (all) {
            result = commandProcessor.getSession().getJshell().snippets()
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        } else if (start) {
            result = commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> Integer.parseInt(s.id()) <= commandProcessor.getSession().getStartSnippetMaxIndex())
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        } else {

            result = commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> commandProcessor.getSession().getJshell().status(s).isActive())
                    .filter(s -> Integer.parseInt(s.id()) > commandProcessor.getSession().getStartSnippetMaxIndex())
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        }

        commandProcessor.getSession().getFeedback().normaln(result);
    }
}

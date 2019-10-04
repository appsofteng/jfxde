package dev.jfxde.sysapps.jshell.commands;

import java.util.stream.Collectors;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.Snippet.Kind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/methods")
public class MethodCommand extends BaseCommand {

    public MethodCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Option(names = "-all", descriptionKey = "/method.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/method.-start")
    private boolean start;

    @Override
    public void run() {

        if (all && start) {
            commandProcessor.getCommandLine().getErr().println(commandProcessor.getSession().getContext().rc().getString("onlyOneOptionAllowed") + "\n");
            return;
        }

        String result = "";
        if (all) {
            result = commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> s.kind() == Kind.METHOD)
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        } else if (start) {
            result = commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> s.kind() == Kind.METHOD)
                    .filter(s -> Integer.parseInt(s.id()) <= commandProcessor.getSession().getStartSnippetMaxIndex())
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        } else {

            result = commandProcessor.getSession().getJshell().snippets()
                    .filter(s -> commandProcessor.getSession().getJshell().status(s).isActive())
                    .filter(s -> s.kind() == Kind.METHOD)
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        }

        commandProcessor.getSession().getFeedback().normaln(result);
    }
}

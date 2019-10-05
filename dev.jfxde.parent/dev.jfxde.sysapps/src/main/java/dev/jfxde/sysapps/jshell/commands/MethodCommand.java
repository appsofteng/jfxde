package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.Snippet.Kind;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/methods")
public class MethodCommand extends BaseCommand {

    @Parameters(paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]", descriptionKey = "/method.ids")
    private ArrayList<String> parameters;

    @Option(names = "-all", descriptionKey = "/method.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/method.-start")
    private boolean start;

    public MethodCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (Stream.of(parameters!= null && !parameters.isEmpty(), all, start).filter(o -> o).count() > 1) {
            commandProcessor.getCommandLine().getErr()
                    .println(commandProcessor.getSession().getContext().rc().getString("onlyOneOptionAllowed") + "\n");
            return;
        }

        String result = "";

        if (parameters != null && !parameters.isEmpty()) {
            result = commandProcessor.matches(parameters).stream()
                    .filter(s -> s.kind() == Kind.METHOD)
                    .map(s -> SnippetUtils.toString(s, commandProcessor.getSession().getJshell()))
                    .collect(Collectors.joining());
        } else if (all) {
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

        commandProcessor.getSession().getFeedback().normal(result);
    }
}

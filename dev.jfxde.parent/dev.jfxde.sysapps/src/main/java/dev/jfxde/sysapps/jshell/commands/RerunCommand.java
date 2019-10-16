package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = RerunCommand.RERUN_COMMAND)
public class RerunCommand extends BaseCommand {

    static final String RERUN_COMMAND = "/rerun";

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public RerunCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    public static String[] setIfMatches(String[] args) {
        String[] newArgs = args;

        if (args[0].matches("/(\\d+|\\d+-\\d+)( (\\d+|\\d+-\\d+|\\w+))*")) {
            newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = RERUN_COMMAND;
            newArgs[1] = args[0].substring(1);
        }

        return newArgs;
    }

    @Override
    public void run() {

        List<Snippet> snippets = commandProcessor.matches(parameters);

        commandProcessor.getSession().getSnippetProcessor().process(snippets);
    }
}

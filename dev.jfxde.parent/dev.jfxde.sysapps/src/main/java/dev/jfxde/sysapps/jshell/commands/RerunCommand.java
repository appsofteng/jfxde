package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.sysapps.jshell.CommandOutput;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = RerunCommand.RERUN_NAME)
public class RerunCommand extends BaseCommand {

    static final String RERUN_NAME = "/rerun";

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public RerunCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }


    public static String[] setIfMatches(String[] args) {
        String[] newArgs = args;

        if (args[0].matches("/(\\d+|\\d+-\\d+)( (\\d+|\\d+-\\d+|\\w+))*")) {
            newArgs = new String[args.length + 1];
            System.arraycopy(args, 0, newArgs, 1, args.length);
            newArgs[0] = RERUN_NAME;
            newArgs[1] = args[0].substring(1);
        }

        return newArgs;
    }

    @Override
    public void run() {

        List<Snippet> snippets = snippetMatch.matches(parameters);

        snippetOutput.output(snippets);
    }
}

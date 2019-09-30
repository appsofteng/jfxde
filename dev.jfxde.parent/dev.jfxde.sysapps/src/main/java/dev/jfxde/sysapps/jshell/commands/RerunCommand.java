package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.SnippetOutput;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = RerunCommand.RERUN_NAME)
public class RerunCommand extends BaseCommand {

    static final String RERUN_NAME = "/id(s)";

    private final SnippetOutput snippetOutput;
    private SnippetMatch snippetMatch;

    @Parameters
    private ArrayList<String> parameters;

    public RerunCommand(AppContext context, JShell jshell, ConsoleModel consoleModel, SnippetOutput snippetOutput, SnippetMatch snippetMatch) {
        super(context, jshell, consoleModel);
        this.snippetOutput = snippetOutput;
        this.snippetMatch = snippetMatch;
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

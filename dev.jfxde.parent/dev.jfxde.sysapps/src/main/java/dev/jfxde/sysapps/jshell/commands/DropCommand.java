package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/drop")
public class DropCommand extends BaseCommand {

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public DropCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {

        List<Snippet> snippets = snippetMatch.matches(parameters);

        StringBuilder sb = new StringBuilder();
        snippets.forEach(s -> {
            if (jshell.status(s) == Status.VALID) {
                sb.append(context.rc().getString("dropped") + SnippetUtils.toString(s, jshell));
                jshell.drop(s);
            } else {
                sb.append(context.rc().getString("notValid") + SnippetUtils.toString(s, jshell));
            }
        });

        if (sb.length() == 0) {
            sb.append(context.rc().getString("noSuchSnippet") + "\n");
        }

        consoleModel.addNewLineOutput(new TextStyleSpans(sb.toString(), ConsoleModel.COMMENT_STYLE));
    }
}

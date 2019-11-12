package dev.jfxde.sysapps.jshell.commands;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.jfx.scene.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.CommandProcessor;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/drop")
public class DropCommand extends BaseCommand {

    @Parameters(arity = "1..*", paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]")
    private ArrayList<String> parameters;

    public DropCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        drop(commandProcessor.matches(parameters));
    }

    public void drop(List<Snippet> snippets) {
        StringBuilder sb = new StringBuilder();
        snippets.forEach(s -> {
            if (commandProcessor.getSession().getJshell().status(s) == Status.VALID) {
                sb.append(commandProcessor.getSession().getContext().rc().getString("dropped") + SnippetUtils.toString(s, commandProcessor.getSession().getJshell()));
                commandProcessor.getSession().getJshell().drop(s);
            } else {
                sb.append(commandProcessor.getSession().getContext().rc().getString("notValid") + SnippetUtils.toString(s, commandProcessor.getSession().getJshell()));
            }
        });

        if (sb.length() == 0) {
            sb.append(commandProcessor.getSession().getContext().rc().getString("noSuchSnippet") + "\n");
        }

        commandProcessor.getSession().getFeedback().normal(sb.toString(), ConsoleModel.COMMENT_STYLE);
    }
}

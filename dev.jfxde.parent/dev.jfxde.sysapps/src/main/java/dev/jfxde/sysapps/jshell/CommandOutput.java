package dev.jfxde.sysapps.jshell;

import java.io.PrintWriter;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.commands.Commands;
import dev.jfxde.sysapps.jshell.commands.DropCommand;
import dev.jfxde.sysapps.jshell.commands.HistoryCommand;
import dev.jfxde.sysapps.jshell.commands.ImportCommand;
import dev.jfxde.sysapps.jshell.commands.ListCommand;
import dev.jfxde.sysapps.jshell.commands.MethodCommand;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import dev.jfxde.sysapps.jshell.commands.SnippetMatch;
import dev.jfxde.sysapps.jshell.commands.TypeCommand;
import dev.jfxde.sysapps.jshell.commands.VarCommand;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class CommandOutput extends JShellOutput {

    private SnippetMatch snippetMatch;
    private SnippetOutput snippetOutput;
    private CommandFactory commandFactory = new CommandFactory();
    private CommandLine commandLine;

    CommandOutput(AppContext context, JShell jshell, ConsoleModel consoleModel, ObservableList<String> history, SnippetOutput snippetOutput) {
        super(context, jshell, consoleModel, history);

        this.snippetOutput = snippetOutput;
        this.snippetMatch = new SnippetMatch(jshell);
        this.commandLine = new CommandLine(new Commands(), commandFactory)
                .setOut(new PrintWriter(consoleModel.getOut(), true))
                .setErr(new PrintWriter(consoleModel.getOut(), true));

    }

    @Override
    void output(String input) {

        String[] args = input.split(" +");

        if (args.length > 0) {

            args = RerunCommand.setIfMatches(args);
        }

        commandLine.execute(args);

    }

    boolean isCommand(String input) {
        return input.matches("/[\\w!?\\-]*( .*)*");
    }

    private class CommandFactory implements IFactory {

        @SuppressWarnings("unchecked")
        @Override
        public <K> K create(Class<K> cls) throws Exception {

            K obj = null;

            if (cls == DropCommand.class) {
                obj = (K) new DropCommand(context, jshell, consoleModel, snippetMatch);
            } else if (cls == ListCommand.class) {
                obj = (K) new ListCommand(context, jshell, consoleModel);
            } else if (cls == HistoryCommand.class) {
                obj = (K) new HistoryCommand(context, jshell, consoleModel, history);
            } else if (cls == ImportCommand.class) {
                obj = (K) new ImportCommand(context, jshell, consoleModel);
            } else if (cls == MethodCommand.class) {
                obj = (K) new MethodCommand(context, jshell, consoleModel);
            } else if (cls == RerunCommand.class) {
                obj = (K) new RerunCommand(context, jshell, consoleModel, snippetOutput, snippetMatch);
            } else if (cls == TypeCommand.class) {
                obj = (K) new TypeCommand(context, jshell, consoleModel);
            } else if (cls == VarCommand.class) {
                obj = (K) new VarCommand(context, jshell, consoleModel);
            } else {
                obj = cls.getConstructor().newInstance();
            }

            return obj;
        }

    }
}

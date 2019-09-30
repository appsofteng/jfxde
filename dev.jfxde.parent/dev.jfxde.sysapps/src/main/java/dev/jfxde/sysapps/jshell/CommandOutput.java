package dev.jfxde.sysapps.jshell;

import java.io.PrintWriter;

import dev.jfxde.sysapps.jshell.commands.Commands;
import dev.jfxde.sysapps.jshell.commands.DropCommand;
import dev.jfxde.sysapps.jshell.commands.HistoryCommand;
import dev.jfxde.sysapps.jshell.commands.ImportCommand;
import dev.jfxde.sysapps.jshell.commands.ListCommand;
import dev.jfxde.sysapps.jshell.commands.MethodCommand;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import dev.jfxde.sysapps.jshell.commands.SaveCommand;
import dev.jfxde.sysapps.jshell.commands.SnippetMatch;
import dev.jfxde.sysapps.jshell.commands.TypeCommand;
import dev.jfxde.sysapps.jshell.commands.VarCommand;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class CommandOutput extends JShellOutput {

    public SnippetMatch snippetMatch;
    CommandFactory commandFactory = new CommandFactory();
    private CommandLine commandLine;

    CommandOutput(JShellContent jshellContent) {
        super(jshellContent);

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
                obj = (K) new DropCommand(CommandOutput.this);
            } else if (cls == ListCommand.class) {
                obj = (K) new ListCommand(CommandOutput.this);
            } else if (cls == HistoryCommand.class) {
                obj = (K) new HistoryCommand(CommandOutput.this);
            } else if (cls == ImportCommand.class) {
                obj = (K) new ImportCommand(CommandOutput.this);
            } else if (cls == MethodCommand.class) {
                obj = (K) new MethodCommand(CommandOutput.this);
            } else if (cls == RerunCommand.class) {
                obj = (K) new RerunCommand(CommandOutput.this);
            } else if (cls ==SaveCommand.class) {
                obj = (K) new SaveCommand(CommandOutput.this);
            } else if (cls == TypeCommand.class) {
                obj = (K) new TypeCommand(CommandOutput.this);
            } else if (cls == VarCommand.class) {
                obj = (K) new VarCommand(CommandOutput.this);
            } else {
                obj = cls.getConstructor().newInstance();
            }

            return obj;
        }

    }
}

package dev.jfxde.sysapps.jshell;

import java.io.PrintWriter;

import dev.jfxde.sysapps.jshell.commands.BaseCommand;
import dev.jfxde.sysapps.jshell.commands.Commands;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import dev.jfxde.sysapps.jshell.commands.SnippetMatch;
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

    public CommandLine getCommandLine() {
        return commandLine;
    }

    @Override
    void process(String input) {

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

        @Override
        public <K> K create(Class<K> cls) throws Exception {

            K obj = null;

            if (BaseCommand.class.isAssignableFrom(cls)) {
                obj = cls.getConstructor(CommandOutput.class).newInstance(CommandOutput.this);
            } else {
                obj = cls.getConstructor().newInstance();
            }

            return obj;
        }

    }
}

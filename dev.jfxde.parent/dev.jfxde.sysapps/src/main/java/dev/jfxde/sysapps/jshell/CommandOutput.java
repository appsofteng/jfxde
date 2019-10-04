package dev.jfxde.sysapps.jshell;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
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
    private PrintWriter out = new PrintWriter(consoleModel.getOut(ConsoleModel.COMMENT_STYLE), true);
    private Map<String, String> subcommandHelps;

    CommandOutput(JShellContent jshellContent) {
        super(jshellContent);

        this.snippetMatch = new SnippetMatch(jshell);
        this.commandLine = new CommandLine(new Commands(), commandFactory)
                .setOut(out)
                .setErr(new PrintWriter(consoleModel.getErr(), true))
                .setResourceBundle(context.rc().getStringBundle());

        // load and cache in parallel
        subcommandHelps = commandLine.getSubcommands().entrySet().parallelStream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> AccessController.doPrivileged((PrivilegedAction<String>) () -> "<pre>" + e.getValue().getUsageMessage() + "</pre>")));

    }

    public CommandLine getCommandLine() {
        return commandLine;
    }

    public Map<String, String> getSubcommandHelps() {
        return subcommandHelps;
    }

    public PrintWriter getOut() {
        return out;
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

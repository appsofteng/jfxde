package dev.jfxde.sysapps.jshell;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.sysapps.jshell.commands.Commands;
import dev.jfxde.sysapps.jshell.commands.DropCommand;
import dev.jfxde.sysapps.jshell.commands.EnvCommand;
import dev.jfxde.sysapps.jshell.commands.HelpCommand;
import dev.jfxde.sysapps.jshell.commands.HistoryCommand;
import dev.jfxde.sysapps.jshell.commands.ImportCommand;
import dev.jfxde.sysapps.jshell.commands.ListCommand;
import dev.jfxde.sysapps.jshell.commands.MethodCommand;
import dev.jfxde.sysapps.jshell.commands.OpenCommand;
import dev.jfxde.sysapps.jshell.commands.ReloadCommand;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import dev.jfxde.sysapps.jshell.commands.ResetCommand;
import dev.jfxde.sysapps.jshell.commands.SaveCommand;
import dev.jfxde.sysapps.jshell.commands.SetCommand;
import dev.jfxde.sysapps.jshell.commands.TypeCommand;
import dev.jfxde.sysapps.jshell.commands.VarCommand;
import javafx.concurrent.Task;
import jdk.jshell.Snippet;
import picocli.CommandLine;

public class CommandProcessor extends Processor {

    private CommandLine commandLine;
    private PrintWriter out;
    private DropCommand dropCommand;
    private Task<CommandLine> task;

    CommandProcessor(Session session) {
        super(session);

        task = session.getContext().tc().execute(TaskUtils.createTask(this::createCommands));
    }

    private CommandLine createCommands() {
        this.out = new PrintWriter(session.getConsoleModel().getOut(ConsoleModel.COMMENT_STYLE), true);
        CommandLine commandLine = new CachingCommandLine(new Commands())
                .addSubcommand(new CachingCommandLine(dropCommand = new DropCommand(this)))
                .addSubcommand(new CachingCommandLine(new EnvCommand(this)))
                .addSubcommand(new CachingCommandLine(new HelpCommand(this)))
                .addSubcommand(new CachingCommandLine(new HistoryCommand(this)))
                .addSubcommand(new CachingCommandLine(new ImportCommand(this)))
                .addSubcommand(new CachingCommandLine(new ListCommand(this)))
                .addSubcommand(new CachingCommandLine(new MethodCommand(this)))
                .addSubcommand(new CachingCommandLine(new OpenCommand(this)))
                .addSubcommand(new CachingCommandLine(new ReloadCommand(this)))
                .addSubcommand(new CachingCommandLine(new RerunCommand(this)))
                .addSubcommand(new CachingCommandLine(new ResetCommand(this)))
                .addSubcommand(new CachingCommandLine(new SaveCommand(this)))
                .addSubcommand(new CachingCommandLine(new SetCommand(this)))
                .addSubcommand(new CachingCommandLine(new TypeCommand(this)))
                .addSubcommand(new CachingCommandLine(new VarCommand(this)))
                .setOut(out)
                .setErr(new PrintWriter(session.getConsoleModel().getErr(), true))
                .setResourceBundle(session.getContext().rc().getStringBundle());

        Map<String, CommandLine> commands = new HashMap<>(commandLine.getSubcommands());
        commands.put("", commandLine);
        // load and cache in parallel
        commands.entrySet().parallelStream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> AccessController.doPrivileged((PrivilegedAction<String>) () -> e.getValue().getUsageMessage())));
        return commandLine;
    }

    public void drop(List<Snippet> snippets) {
        dropCommand.drop(snippets);
    }

    public List<Snippet> matches(String[] values) {
        return matches(Arrays.asList(values));
    }

    public List<Snippet> matches(List<String> values) {

        List<Snippet> snippets = new ArrayList<>();

        for (String value : values) {
            if (value.matches("\\d+")) {
                Snippet s = session.getSnippetsById().get(value);
                if (s != null) {
                    snippets.add(s);
                }
            } else if (value.matches("\\d+-\\d+")) {
                String[] parts = value.split("-");
                snippets.addAll(IntStream.rangeClosed(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]))
                        .mapToObj(i -> session.getSnippetsById().get(String.valueOf(i)))
                        .filter(s -> s != null)
                        .collect(Collectors.toList()));
            } else {
                snippets.addAll(session.getSnippetsByName().getOrDefault(value, List.of()));
            }
        }

        return snippets;
    }

    public CommandLine getCommandLine() {

        if (commandLine == null) {
            try {
                commandLine = task.get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        return commandLine;
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

        getCommandLine().execute(args);
    }

    static boolean isCommand(String input) {
        return input.matches("/[\\w!?\\-]*( .*)*");
    }

    private static class CachingCommandLine extends CommandLine {

        private String cache;

        public CachingCommandLine(Object command) {
            super(command);
        }

        @Override
        public String getUsageMessage() {
            return usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), getColorScheme())).toString();
        }

        @Override
        public void usage(PrintStream out, Help.ColorScheme colorScheme) {
            out.print(usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), colorScheme)));
            out.flush();
        }

        @Override
        public void usage(PrintWriter writer, Help.ColorScheme colorScheme) {
            writer.print(usage(new StringBuilder(), getHelpFactory().create(getCommandSpec(), colorScheme)));
            writer.flush();
        }

        private String usage(StringBuilder sb, Help help) {
            if (cache == null) {
                for (String key : getHelpSectionKeys()) {
                    IHelpSectionRenderer renderer = getHelpSectionMap().get(key);
                    if (renderer != null) {
                        sb.append(renderer.render(help));
                    }
                }
                cache = sb.toString();
            }
            return cache;
        }
    }
}

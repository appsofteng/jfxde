package dev.jfxde.sysapps.jshell;

import java.io.PrintWriter;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.commands.BaseCommand;
import dev.jfxde.sysapps.jshell.commands.Commands;
import dev.jfxde.sysapps.jshell.commands.RerunCommand;
import jdk.jshell.Snippet;
import picocli.CommandLine;
import picocli.CommandLine.IFactory;

public class CommandProcessor extends Processor {

    private CommandFactory commandFactory = new CommandFactory();
    private CommandLine commandLine;
    private PrintWriter out;
    private Map<String, String> subcommandHelps;

    CommandProcessor(Session session) {
        super(session);

        this.out = new PrintWriter(session.getConsoleModel().getOut(ConsoleModel.COMMENT_STYLE), true);
        this.commandLine = new CommandLine(new Commands(), commandFactory)
                .setOut(out)
                .setErr(new PrintWriter(session.getConsoleModel().getErr(), true))
                .setResourceBundle(session.getContext().rc().getStringBundle());

        // load and cache in parallel
        subcommandHelps = commandLine.getSubcommands().entrySet().parallelStream()
                .collect(Collectors.toMap(e -> e.getKey(),
                        e -> AccessController.doPrivileged((PrivilegedAction<String>) () -> "<pre>" + e.getValue().getUsageMessage() + "</pre>")));

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

    static boolean isCommand(String input) {
        return input.matches("/[\\w!?\\-]*( .*)*");
    }

    private class CommandFactory implements IFactory {

        @Override
        public <K> K create(Class<K> cls) throws Exception {

            K obj = null;

            if (BaseCommand.class.isAssignableFrom(cls)) {
                obj = cls.getConstructor(CommandProcessor.class).newInstance(CommandProcessor.this);
            } else {
                obj = cls.getConstructor().newInstance();
            }

            return obj;
        }

    }
}

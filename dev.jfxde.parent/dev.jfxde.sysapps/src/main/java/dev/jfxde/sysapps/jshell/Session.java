package dev.jfxde.sysapps.jshell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.logic.JsonUtils;
import dev.jfxde.sysapps.jshell.Feedback.Mode;
import io.vavr.Tuple2;
import javafx.stage.Window;
import jdk.jshell.JShell;
import jdk.jshell.JShell.Subscription;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;

public class Session {

    private static final String ENV_FILE_NAME = "env.json";
    private static final String SETTINGS_FILE_NAME = "sessings.json";

    private Env env;
    private Settings settings;
    private Feedback feedback;
    private AppContext context;
    private JShellContent content;
    private JShell jshell;
    private ConsoleModel consoleModel;
    private List<String> history = new ArrayList<>();
    private IdGenerator idGenerator;
    private CommandProcessor commandProcessor;
    private SnippetProcessor snippetProcessor;
    private int startSnippetMaxIndex;
    private Map<String, Snippet> snippetsById = new HashMap<>();
    private Map<String, List<Snippet>> snippetsByName = new HashMap<>();
    private Subscription subscription;

    public Session(JShellContent content) {
        this.context = content.getContext();
        this.content = content;
        this.consoleModel = content.getConsoleView().getConsoleModel();

        feedback = new Feedback(consoleModel);
        commandProcessor = new CommandProcessor(this);
        snippetProcessor = new SnippetProcessor(this);
        env = loadEnv();
        settings = loadSettings();
        idGenerator = new IdGenerator();
        reset();
        setListener();
    }

    public JShellContent getContent() {
        return content;
    }

    public Feedback getFeedback() {
        return feedback;
    }

    public AppContext getContext() {
        return context;
    }

    public JShell getJshell() {
        return jshell;
    }

    SplitConsoleView getConsoleView() {
        return content.getConsoleView();
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public List<String> getHistory() {
        return history;
    }

    public CommandProcessor getCommandProcessor() {
        return commandProcessor;
    }

    public SnippetProcessor getSnippetProcessor() {
        return snippetProcessor;
    }

    public int getStartSnippetMaxIndex() {
        return startSnippetMaxIndex;
    }

    public void setStartSnippetMaxIndex(int startSnippetMaxIndex) {
        this.startSnippetMaxIndex = startSnippetMaxIndex;
    }

    public Window getWindow() {
        return content.getScene().getWindow();
    }

    public Map<String, Snippet> getSnippetsById() {
        return snippetsById;
    }

    public Map<String, List<Snippet>> getSnippetsByName() {
        return snippetsByName;
    }

    public Env loadEnv() {
        return JsonUtils.fromJson(context.fc().getAppDataDir().resolve(ENV_FILE_NAME), Env.class, new Env("default"));
    }

    private void setEnv(Env env) {
        this.env = env;
        JsonUtils.toJson(env, context.fc().getAppDataDir().resolve(ENV_FILE_NAME));
    }

    public void resetEnv(Env env) {
        setEnv(env);
        reset();
    }

    public void reloadEnv(Env env) {
        setEnv(env);
        reload(Mode.SILENT);
    }


    public Settings loadSettings() {
        return JsonUtils.fromJson(context.fc().getAppDataDir().resolve(SETTINGS_FILE_NAME), Settings.class, new Settings());
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
        JsonUtils.toJson(settings, context.fc().getAppDataDir().resolve(SETTINGS_FILE_NAME));
    }

    private void setListener() {
        subscription = jshell.onSnippetEvent(e -> {

            if (e.snippet() == null || e.snippet().id() == null) {
                return;
            }

            String name = SnippetUtils.getName(e.snippet());

            snippetsById.put(e.snippet().id(), e.snippet());
            List<Snippet> snippets = snippetsByName.computeIfAbsent(name, k -> new ArrayList<>());
            snippets.add(e.snippet());
        });
    }

    public void reset() {
        snippetsById.clear();
        snippetsByName.clear();
        feedback.setMode(Mode.SILENT);
        buildJShell();
        setListener();
        if (settings.isLoadDefault()) {
            loadDefault();
        }

        if (settings.isLoadPrinting()) {
            loadPrinting();
        }

        if (settings.isLoadScripts()) {
            loadStartupScripts();
        }

        startSnippetMaxIndex = idGenerator.getMaxId();
        feedback.setMode(Mode.NORMAL);
    }

    public void reload() {
        reload(Mode.NORMAL);
    }

    private void reload(Mode mode) {

        feedback.setMode(mode);
        List<Tuple2<Snippet, Status>> snippets = jshell.snippets()
                .filter(s -> jshell.status(s) == Status.VALID || jshell.status(s) == Status.DROPPED)
                .map(s -> new Tuple2<>(s, jshell.status(s)))
                .collect(Collectors.toList());
        reset();

        snippets.forEach(s -> {
            var newSnippets = snippetProcessor.getSnippetEvents(s._1().source()).stream().map(SnippetEvent::snippet).collect(Collectors.toList());
            if (s._2() == Status.DROPPED) {
                commandProcessor.drop(newSnippets);
            }
        });

        feedback.setMode(Mode.NORMAL);
    }

    private void buildJShell() {

        close();
        try {
            String[] options = env.getOptions();
            jshell = JShell.builder()
                    .idGenerator(idGenerator)
                    .in(consoleModel.getIn())
                    .out(consoleModel.getOut())
                    .err(consoleModel.getErr())
                    .compilerOptions(options)
                    .remoteVMOptions(options)
                    .build();
            // Create the analysis before putting on the class path.
            jshell.sourceCodeAnalysis();
            env.getClassPath().forEach(p -> jshell.addToClasspath(p));
            env.getModuleLocations().forEach(p -> jshell.addToClasspath(p));
            idGenerator.setJshell(jshell);

        } catch (Exception e) {
            e.printStackTrace(consoleModel.getErr());
        }
    }

    void close() {

        if (jshell != null) {
            if (subscription != null) {
                jshell.unsubscribe(subscription);
            }

            jshell.stop();
            jshell.close();
        }
    }

    public void loadDefault() {
        try {
            JShellUtils.loadSnippets(jshell, getClass().getResourceAsStream("start-default.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadPrinting() {

        try {
            JShellUtils.loadSnippets(jshell, getClass().getResourceAsStream("start-printing.txt"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadStartupScripts() {

        for (String file : settings.getStartupScripts()) {
            Path path = Paths.get(file);
            if (Files.exists(path)) {
                try {
                    String spippets = Files.readString(path);
                    commandProcessor.getSession().process(spippets);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void processAsync(String input) {
        context.tc().executeSequentially(() -> {
            feedback.setCached(true);
            process(input);
            feedback.flush();
        });
    }

    public void process(String input) {

        if (input.isBlank()) {
            return;
        }

        history.add(input.strip());

        String[] lines = input.split("\n");
        StringBuilder sb = new StringBuilder();

        for (String line : lines) {

            if (CommandProcessor.isCommand(line)) {
                if (sb.length() > 0) {
                    String snippets = sb.toString();
                    snippetProcessor.process(snippets);
                    sb.delete(0, sb.length());
                }
                commandProcessor.process(line);
            } else {
                sb.append(line).append("\n");
            }
        }

        if (sb.length() > 0) {
            snippetProcessor.process(sb.toString());
        }
    }
}

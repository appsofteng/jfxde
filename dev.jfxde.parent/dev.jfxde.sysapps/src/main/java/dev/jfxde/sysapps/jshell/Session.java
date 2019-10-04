package dev.jfxde.sysapps.jshell;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.Feedback.Mode;
import javafx.collections.ObservableList;
import javafx.stage.Window;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class Session {

    private Env env;
    private Settings settings;
    private Feedback feedback;
    private AppContext context;
    private JShellContent content;
    private JShell jshell;
    private ConsoleModel consoleModel;
    private ObservableList<String> history;
    private IdGenerator idGenerator;
    private CommandProcessor commandProcessor;
    private SnippetProcessor snippetProcessor;
    private int startSnippetMaxIndex;
    private Map<String, Snippet> snippetsById = new HashMap<>();
    private Map<String, List<Snippet>> snippetsByName = new HashMap<>();

    public Session(AppContext context, JShellContent content, ConsoleModel consoleModel, ObservableList<String> history) {
        this.context = context;
        this.content = content;
        this.consoleModel = consoleModel;
        this.history = history;

        feedback = new Feedback(consoleModel);
        env = loadEnv();
        settings = loadSettings();
        idGenerator = new IdGenerator();
        reset();
        setListener();

        commandProcessor = new CommandProcessor(this);
        snippetProcessor = new SnippetProcessor(this);
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

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public ObservableList<String> getHistory() {
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

    private Env loadEnv() {
        return new Env();
    }

    private Settings loadSettings() {
        return new Settings();
    }

    private void setListener() {
        jshell.onSnippetEvent(e -> {

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
        if (settings.isLoadDefault()) {
            loadDefault();
        }

        if (settings.isLoadPrinting()) {
            loadPrinting();
        }

        loadFiles();
        startSnippetMaxIndex = idGenerator.getMaxId();
        feedback.setMode(Mode.NORMAL);
    }

    private void buildJShell() {

        if (jshell != null) {
            jshell.close();
        }
        jshell = JShell.builder()
                .idGenerator(idGenerator)
                .in(consoleModel.getIn())
                .out(consoleModel.getOut())
                .err(consoleModel.getErr())
                .compilerOptions(env.getOptions())
                .build();
        jshell.sourceCodeAnalysis();
        idGenerator.setJshell(jshell);
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

    private void loadFiles() {

        for (String file : settings.getLoadFiles()) {
            Path path = Paths.get(file);
            if (Files.exists(path)) {
                try {
                    String spippets = Files.readString(path);
                    commandProcessor.getSession().getSnippetProcessor().process(spippets);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
}

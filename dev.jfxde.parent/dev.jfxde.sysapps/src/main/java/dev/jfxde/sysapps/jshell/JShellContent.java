package dev.jfxde.sysapps.jshell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.jfxext.control.editor.CompletionBehavior;
import dev.jfxde.jfxext.control.editor.CompletionItem;
import dev.jfxde.jfxext.control.editor.DocRef;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.jfxext.util.JavadocUtils;
import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.logic.JsonUtils;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Task;
import javafx.scene.layout.BorderPane;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;

public class JShellContent extends BorderPane {

    private static final String HISTORY_FILE_NAME = "history.json";

    private static final Logger LOGGER = Logger.getLogger(JShellContent.class.getName());

    AppContext context;
    SplitConsoleView consoleView;
    JShell jshell;
    SnippetOutput snippetOutput;
    CommandOutput commandOutput;

    public JShellContent(AppContext context) {
        this.context = context;
        consoleView = new SplitConsoleView(loadHistory());
        setCenter(consoleView);
        setBehavior();
        IdGenerator idGenerator = new IdGenerator();
        jshell = JShell.builder().idGenerator(idGenerator)
                .in(consoleView.getConsoleModel().getIn())
                .out(consoleView.getConsoleModel().getOut())
                .err(consoleView.getConsoleModel().getErr())
                .build();
        jshell.sourceCodeAnalysis();
        idGenerator.setJshell(jshell);

        snippetOutput = new SnippetOutput(this);
        commandOutput = new CommandOutput(this);

        loadHistory();
        loadStartSnippets();
    }

    private void setBehavior() {

        consoleView.getConsoleModel().getInputToOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    for (TextStyleSpans span : added) {
                        enter(span.getText());
                    }
                }
            }
        });

        consoleView.getHistory().addListener((Change<? extends String> c) -> {

            while (c.next()) {

                if (c.wasAdded() || c.wasRemoved()) {
                    List<? extends String> history = new ArrayList<>(consoleView.getHistory());
                    context.tc().executeSequentially(TaskUtils
                            .createTask(() -> JsonUtils.toJson(history, context.fc().getAppDataDir().resolve(HISTORY_FILE_NAME))));
                }
            }
        });

        consoleView.getEditor().add(new CompletionBehavior<>(this::codeCompletion, this::loadDocumentation));
    }

    private List<String> loadHistory() {
        @SuppressWarnings("unchecked")
        List<String> history = JsonUtils.fromJson(context.fc().getAppDataDir().resolve(HISTORY_FILE_NAME), List.class, List.of());
        return history;
    }

    private void loadStartSnippets() {

        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("start-snippets.txt")));
            reader.lines().forEach(s -> jshell.eval(s));
        } catch (Exception e) {

            LOGGER.log(Level.WARNING, e.getMessage(), e);
        }
    }

    private void enter(String input) {

        // Null char may come from clipboard.
        input = input.strip().replace("\0", "");

        if (input.isBlank()) {
            return;
        }

        Task<Void> task = getTask(input);
        context.tc().executeSequentially(task);
    }

    private void codeCompletion(CompletionBehavior<CodeArea> behavior) {

        context.tc().executeSequentially(TaskUtils.createTask(() -> getCompletionItems(behavior.getArea()), behavior::showCompletionItems));
    }

    private String loadDocumentation(DocRef docRef) {
        Map<String, String> docBlockNames = context.rc().getStrings(JavadocUtils.getBlockTagNames());
        String documentation = JShellUtils.getDocumentation(jshell, docRef, docBlockNames);

        return documentation;
    }

    private Collection<CompletionItem> getCompletionItems(CodeArea inputArea) {

        return inputArea.getText().isBlank() || commandOutput.isCommand(inputArea.getText()) ? getCommandCompletionItems(inputArea)
                : getCodeCompletionItems(inputArea);
    }

    private Collection<CompletionItem> getCommandCompletionItems(CodeArea inputArea) {
        return List.of();
    }

    private Collection<CompletionItem> getCodeCompletionItems(CodeArea inputArea) {
        List<CompletionItem> items = new ArrayList<>();

        String code = inputArea.getText();
        int cursor = inputArea.getCaretPosition();

        int[] anchor = new int[1];

        Set<SuggestionCompletionItem> suggestionItems = jshell.sourceCodeAnalysis()
                .completionSuggestions(code, cursor, anchor)
                .stream()
                .map(s -> new SuggestionCompletionItem(inputArea, code, s, anchor))
                .collect(Collectors.toSet());

        for (SuggestionCompletionItem item : suggestionItems) {

            List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(item.getDocRef().getDocCode(),
                    item.getDocRef().getDocCode().length(), false);

            if (docs.isEmpty()) {
                items.add(item);
            }

            for (Documentation doc : docs) {
                items.add(new SuggestionCompletionItem(inputArea, item.getSuggestion(), item.getAnchor(), item.getDocRef().getDocCode(),
                        doc.signature()));
            }
        }

        Collections.sort(items);

        QualifiedNames qualifiedNames = jshell.sourceCodeAnalysis().listQualifiedNames(code, cursor);

        if (!qualifiedNames.isResolvable()) {
            Set<CompletionItem> names = qualifiedNames.getNames()
                    .stream()
                    .map(n -> new QualifiedNameCompletionItem(consoleView.getConsoleModel().getInput(), n))
                    .sorted()
                    .collect(Collectors.toSet());

            items.addAll(names);
        }

        return items;
    }

    private Task<Void> getTask(String input) {

        JShellOutput output = commandOutput.isCommand(input) ? commandOutput : snippetOutput;

        Task<Void> task = TaskUtils.createTask(() -> output.output(input));

        return task;
    }

    public void stop() {
        context.tc().executeSequentially(TaskUtils.createTask(() -> jshell.close(), () -> consoleView.dispose()));
    }
}

package dev.jfxde.sysapps.jshell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.TaskUtils;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.Documentation;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;

public class JShellContent extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(JShellContent.class.getName());

    private SplitConsoleView consoleView;
    private JShell jshell;
    private CodeCompletionPopup codeCompletion;
    private SnippetOutput snippetOutput;
    private CommandOutput commandOutput;

    public JShellContent(AppContext context) {

        consoleView = new SplitConsoleView();
        setCenter(consoleView);
        setBehavior();
        IdGenerator idGenerator = new IdGenerator();
        jshell = JShell.builder().idGenerator(idGenerator)
                .in(consoleView.getConsoleModel().getIn())
                .out(consoleView.getConsoleModel().getOut())
                .err(consoleView.getConsoleModel().getErr())
                .build();

        idGenerator.setJshell(jshell);

        snippetOutput = new SnippetOutput(context, jshell, consoleView.getConsoleModel().getOutput());
        commandOutput = new CommandOutput(context, jshell, consoleView.getConsoleModel().getOutput(), consoleView.getHistory(), snippetOutput);

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

        consoleView.getInputArea().addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
                codeCompletion();
            }
        });

        consoleView.getInputArea().caretPositionProperty().addListener((v, o, n) -> {
            if (codeCompletion != null) {
                codeCompletion();
            }
        });
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
        Sys.tm().executeSequentially(task);
    }

    private void codeCompletion() {

        if (codeCompletion != null) {
            codeCompletion.close();
        }

        Sys.tm().executeSequentially(TaskUtils.createTask(this::getCompletionItems, this::showCompletionItems));
    }

    private Collection<CompletionItem> getCompletionItems() {
        List<CompletionItem> items = new ArrayList<>();

        String code = consoleView.getInputArea().getText();
        int cursor = consoleView.getInputArea().getCaretPosition();

        int[] anchor = new int[1];

        Set<SuggestionCompletionItem> suggestionItems = jshell.sourceCodeAnalysis()
                .completionSuggestions(code, cursor, anchor)
                .stream()
                .map(s -> new SuggestionCompletionItem(consoleView.getInputArea(),code, s, anchor))
                .collect(Collectors.toSet());

        for (SuggestionCompletionItem item : suggestionItems) {

            List<Documentation> docs = jshell.sourceCodeAnalysis().documentation(item.getDocCode(), item.getDocCode().length(), false);

            if (docs.isEmpty()) {
                items.add(item);
            }

            for (Documentation doc : docs) {
                items.add(new SuggestionCompletionItem(consoleView.getInputArea(), item.getSuggestion(), item.getAnchor(), item.getDocCode(),
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

    private void showCompletionItems(Collection<CompletionItem> items) {
        codeCompletion = new CodeCompletionPopup(items);

        Optional<Bounds> boundsOption = consoleView.getInputArea().caretBoundsProperty().getValue();

        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            codeCompletion.show(consoleView.getInputArea(), bounds.getMaxX(), bounds.getMaxY());
            codeCompletion.setOnHidden(ev -> {
                CompletionItem selection = codeCompletion.getSelection();
                codeCompletion = null;
                if (selection != null) {
                    selection.complete();
                }
            });
        }
    }

    private Task<Void> getTask(String input) {

        JShellOutput output = input.startsWith("/") ? commandOutput : snippetOutput;

        Task<Void> task = TaskUtils.createTask(() -> output.output(input));

        return task;
    }

    public void stop() {
        consoleView.dispose();
        jshell.close();
    }
}

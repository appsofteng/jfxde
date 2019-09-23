package dev.jfxde.sysapps.xjshell;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.logic.ConsoleManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.TaskUtils;
import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import dev.jfxde.sysapps.util.ContextMenuBuilder;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Task;
import javafx.geometry.Bounds;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import jdk.jshell.JShell;
import jdk.jshell.SourceCodeAnalysis.QualifiedNames;

public class XJShellContent extends BorderPane {

    private static final Logger LOGGER = Logger.getLogger(XJShellContent.class.getName());

    private SplitConsoleView consoleView;
    private CodeArea inputArea = new CodeArea();
    private CodeArea outputArea = new CodeArea();
    private JShell jshell;
    private ConsoleManager consoleManager = new ConsoleManager(false);
    private List<String> history = new ArrayList<>();
    private int historyIndex;
    private CodeCompletionPopup codeCompletion;
    private SnippetOutput snippetOutput;
    private CommandOutput commandOutput;

    public XJShellContent(AppContext context) {

        consoleView = new SplitConsoleView();
        setCenter(consoleView);
        setBehavior();

//        getStylesheets().add(context.rc().getCss("console"));
//        outputArea.setEditable(false);
//        outputArea.getStylesheets().add(context.rc().getCss("code-area"));
//        outputArea.setFocusTraversable(false);
//        ContextMenuBuilder.get(outputArea, context).copy().selectAll().clear();
//
//        inputArea.requestFocus();
//        inputArea.getStylesheets().add(context.rc().getCss("area"));
//        inputArea.setWrapText(true);
//        inputArea.getStyleClass().add("jd-input");
//        ContextMenuBuilder.get(inputArea, context).copy().cut().paste().selectAll();
//
//        SplitPane splitPane = new SplitPane(new VirtualizedScrollPane<>(outputArea), new VirtualizedScrollPane<>(inputArea));
//        splitPane.setOrientation(Orientation.VERTICAL);
//        splitPane.setDividerPositions(0.8f);
//
//        setCenter(splitPane);
//
//        setListeners();
//
//        IdGenerator idGenerator = new IdGenerator();
//        jshell = JShell.builder().idGenerator(idGenerator).out(consoleManager.getCout()).err(consoleManager.getCerr()).build();
//
//        idGenerator.setJshell(jshell);
//        jshell.sourceCodeAnalysis();
//
//        snippetOutput = new SnippetOutput(context, jshell, outputArea);
//        commandOutput = new CommandOutput(context, jshell, outputArea, snippetOutput, history);
//
//        loadStartSnippets();
    }

    private void setBehavior() {
        consoleView.getInput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    for (TextStyleSpans span : added) {
                        consoleView.getOutput().add(span);
                        consoleView.getOutput().add(new TextStyleSpans("\n"));
                    }
                }
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

    private void setListeners() {

        consoleManager.getOutputs().addListener((Change<? extends ConsoleOutput> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends ConsoleOutput> added = new ArrayList<>(c.getAddedSubList());
                    Platform.runLater(() -> {
                        CodeAreaUtils.addOutput(outputArea, added);
                    });
                }
            }
        });

        outputArea.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                inputArea.requestFocus();
            }
        });

        inputArea.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.ENTER && e.isShiftDown()) {
                enter();
            } else if (e.getCode() == KeyCode.UP && e.isControlDown()) {
                historyUp();
            } else if (e.getCode() == KeyCode.DOWN && e.isControlDown()) {
                historyDown();
            } else if (e.getCode() == KeyCode.SPACE && e.isControlDown()) {
                codeCompletion();
            }
        });

        inputArea.sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                inputArea.requestFocus();
            }
        });

        inputArea.caretPositionProperty().addListener((v, o, n) -> {
            if (codeCompletion != null) {
                codeCompletion();
            }
        });
    }

    private void enter() {
        String input = inputArea.getText();
        inputArea.replaceText("");

        // Null char may come from clipboard.
        input = input.replace("\0", "");
        CodeAreaUtils.addOutput(outputArea, input + "\n");

        if (input.isBlank()) {
            return;
        }

        history.add(input);
        historyIndex = history.size();

        Task<Void> task = getTask(input);
        Sys.tm().executeSequentially(task);
    }

    private void historyUp() {

        if (historyIndex > 0 && historyIndex <= history.size()) {
            historyIndex--;
            String item = history.get(historyIndex);
            inputArea.replaceText(item);
        }
    }

    private void historyDown() {

        if (historyIndex >= 0 && historyIndex < history.size() - 1) {
            historyIndex++;
            String item = history.get(historyIndex);
            inputArea.replaceText(item);
        } else {
            inputArea.replaceText("");
            historyIndex = history.size();
        }
    }

    private void codeCompletion() {

        if (codeCompletion != null) {
            codeCompletion.close();
        }

        int[] anchor = new int[1];

        List<CompletionItem> items = jshell.sourceCodeAnalysis()
                .completionSuggestions(inputArea.getText(), inputArea.getCaretPosition(), anchor)
                .stream()
                .map(s -> new SuggestionCompletionItem(inputArea, s, anchor)).collect(Collectors.toList());

        QualifiedNames qualifiedNames = jshell.sourceCodeAnalysis().listQualifiedNames(inputArea.getText(), inputArea.getCaretPosition());

        if (!qualifiedNames.isResolvable()) {
            List<CompletionItem> names = qualifiedNames.getNames().stream()
                    .map(n -> new QualifiedNameCompletionItem(jshell, n)).collect(Collectors.toList());

            items.addAll(names);
        }

        codeCompletion = new CodeCompletionPopup(items);

        Optional<Bounds> boundsOption = inputArea.caretBoundsProperty().getValue();

        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            codeCompletion.show(inputArea, bounds.getMaxX(), bounds.getMaxY());
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

        XJShellOutput output = input.startsWith("/") ? commandOutput : snippetOutput;

        Task<Void> task = TaskUtils.createTask(() -> output.output(input));

        return task;
    }

    public void stop() {
        inputArea.dispose();
        outputArea.dispose();
        jshell.close();
    }
}

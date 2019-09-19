package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.ConsoleManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.TaskUtils;
import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.sysapps.util.CodeAreaUtils;
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
import jdk.jshell.SourceCodeAnalysis.Suggestion;

public class JShellContent extends BorderPane {

    private CodeArea inputArea = new CodeArea();
    private CodeArea outputArea = new CodeArea();
    private JShell jshell;
    private ConsoleManager consoleManager = new ConsoleManager(false);
    private List<String> history = new ArrayList<>();
    private int historyIndex;
    private CodeCompletionPopup codeCompletion;
    private SnippetOutput snippetOutput;
    private CommandOutput commandOutput;

    public JShellContent(AppContext context) {
        getStylesheets().add(context.rc().getCss("console"));
        outputArea.setEditable(false);
        outputArea.getStylesheets().add(context.rc().getCss("code-area"));
        outputArea.setFocusTraversable(false);

        inputArea.requestFocus();
        inputArea.getStylesheets().add(context.rc().getCss("area"));
        inputArea.setWrapText(true);
        inputArea.getStyleClass().add("jd-input");

        SplitPane splitPane = new SplitPane(new VirtualizedScrollPane<>(outputArea), new VirtualizedScrollPane<>(inputArea));
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.7f);

        setCenter(splitPane);

        setListeners();

        IdGenerator idGenerator = new IdGenerator();
        jshell = JShell.builder().idGenerator(idGenerator).out(consoleManager.getCout()).err(consoleManager.getCerr()).build();
        idGenerator.setJshell(jshell);
        jshell.sourceCodeAnalysis();

        initImports();

        snippetOutput = new SnippetOutput(context, jshell, outputArea);
        commandOutput = new CommandOutput(context, jshell, outputArea, history);
    }

    private void initImports() {

        List<String> packages = List.of("java.io.*", "java.math.*", "java.net.*", "java.nio.file.*", "java.util.*", "java.util.concurrent.*",
                "java.util.function.*", "java.util.prefs.*", "java.util.regex.*", "java.util.stream.*");

        packages.forEach(p -> jshell.eval(String.format("import %s;", p)));
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

        outputArea.selectedTextProperty().addListener((v, o, n) -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(outputArea.getSelectedText());
            clipboard.setContent(content);
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

        inputArea.caretPositionProperty().addListener((v, o, n) -> {
            if (codeCompletion != null) {
                codeCompletion();
            }
        });
    }

    private void enter() {
        String input = inputArea.getText();
        inputArea.replaceText("");

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

        List<String> suggestions = jshell.sourceCodeAnalysis()
                .completionSuggestions(inputArea.getText(), inputArea.getCaretPosition(), anchor).stream()
                .map(Suggestion::continuation).collect(Collectors.toList());
        codeCompletion = new CodeCompletionPopup(suggestions);

        Optional<Bounds> boundsOption = inputArea.caretBoundsProperty().getValue();

        if (boundsOption.isPresent()) {
            Bounds bounds = boundsOption.get();
            codeCompletion.show(inputArea, bounds.getMaxX(), bounds.getMaxY());
            codeCompletion.setOnHidden(ev -> {
                String selection = codeCompletion.getSelection();
                codeCompletion = null;
                if (selection != null) {
                    inputArea.replaceText(anchor[0], inputArea.getCaretPosition(), selection);
                }
            });
        }
    }

    private Task<Void> getTask(String input) {

        JShellOutput output = input.startsWith("/") ? commandOutput : snippetOutput;

        Task<Void> task = TaskUtils.createTask(() -> output.output(input));

        return task;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        inputArea.requestFocus();
    }

    public void stop() {
        inputArea.dispose();
        outputArea.dispose();
        jshell.close();
    }
}

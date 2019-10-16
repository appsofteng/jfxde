package dev.jfxde.jfxext.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.jfxext.richtextfx.ContextMenuBuilder;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import dev.jfxde.jfxext.richtextfx.features.AreaFeatures;
import dev.jfxde.jfxext.richtextfx.features.IndentationFeature;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class SplitConsoleView extends BorderPane {

    private static final int HISTORY_LIMIT = 100;
    private ConsoleModel consoleModel;
    private CodeArea inputArea = new CodeArea();
    private CodeArea outputArea = new CodeArea();
    private ObservableList<String> history = FXCollections.observableArrayList();
    private int historyIndex;
    private List<String> styleFilter;

    public SplitConsoleView() {
        this(new ConsoleModel(), List.of(), List.of());
    }

    public SplitConsoleView(List<String> history, List<String> styleFilter) {
        this(new ConsoleModel(), history, styleFilter);
    }

    public SplitConsoleView(ConsoleModel consoleModel) {
        this(consoleModel, List.of(), List.of());
    }

    public SplitConsoleView(ConsoleModel consoleModel, List<String> history, List<String> styleFilter) {
        this.consoleModel = consoleModel;
        this.history.addAll(history);
        this.styleFilter = styleFilter;
        historyIndex = history.size();
        initInputArea();
        setGraphics();
        setBehavior();
    }

    private void initInputArea() {
        AreaFeatures.decorate(inputArea).add(new IndentationFeature<>()).init();
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public ObservableList<String> getHistory() {
        return history;
    }

    private ObservableList<TextStyleSpans> getInput() {
        return consoleModel.getInput();
    }

    private ObservableList<TextStyleSpans> getOutput() {
        return consoleModel.getOutput();
    }

    public CodeArea getInputArea() {
        return inputArea;
    }

    public CodeArea getOutputArea() {
        return outputArea;
    }

    private void setGraphics() {
        String areaCss = getClass().getResource("area.css").toExternalForm();
        outputArea.setEditable(false);
        outputArea.setFocusTraversable(false);
        outputArea.getStylesheets().add(areaCss);
        ContextMenuBuilder.get(outputArea).copy().selectAll().clear();

        inputArea.requestFocus();
        inputArea.getStylesheets().add(areaCss);
        ContextMenuBuilder.get(inputArea).copy().cut().paste().selectAll().clear().separator().undo().redo();

        SplitPane splitPane = new SplitPane(new VirtualizedScrollPane<>(outputArea), new VirtualizedScrollPane<>(inputArea));
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8f);

        setCenter(splitPane);
    }

    @Override
    public String getUserAgentStylesheet() {
        return getClass().getResource("console.css").toExternalForm();
    }

    private void setBehavior() {

        inputArea.sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
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
            }
        });

        outputArea.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                inputArea.requestFocus();
            }
        });

        getOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                    Platform.runLater(() -> {
                        for (TextStyleSpans span : added) {
                            int from = outputArea.getLength();
                            outputArea.appendText(span.getText());
                            outputArea.setStyleSpans(from, span.getStyleSpans());
                        }
                        outputArea.moveTo(outputArea.getLength());
                        outputArea.requestFollowCaret();
                    });
                }
            }
        });
    }

    private void enter() {

        // Null char may come from clipboard.
        if (inputArea.getText().contains("\0")) {
            inputArea.replaceText(inputArea.getText().replace("\0", ""));
        }

        if (outputArea.getLength() > 0 && !outputArea.getText().endsWith("\n\n")) {
            outputArea.appendText("\n");
        }

        inputArea.appendText("\n");

        TextStyleSpans span = new TextStyleSpans(inputArea.getText(), filterStyles());
        getInput().add(span);

        history.add(span.getText().strip());

        if (history.size() > HISTORY_LIMIT) {
            history.remove(0);
        }

        historyIndex = history.size();

        inputArea.clear();
    }

    public void enter(String input) {
        if (outputArea.getLength() > 0 && !outputArea.getText().endsWith("\n\n")) {
            outputArea.appendText("\n");
        }

        getInput().add(new TextStyleSpans(input));
    }

    private StyleSpans<Collection<String>> filterStyles() {
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        inputArea.getStyleSpans(0, inputArea.getText().length()).forEach(s -> {
            if (s.getStyle().contains("block-delimiter-match")) {
                var n = s.getStyle().stream().filter(c -> !styleFilter.contains(c)).collect(Collectors.toList());
                spansBuilder.add(n, s.getLength());
            } else {
                spansBuilder.add(s);
            }
        });

        var styleSpans = spansBuilder.create();

        return styleSpans;
    }

    private void historyUp() {

        if (historyIndex > 0 && historyIndex <= history.size()) {
            historyIndex--;
            String text = history.get(historyIndex);
            inputArea.replaceText(text);
        }
    }

    private void historyDown() {

        if (historyIndex >= 0 && historyIndex < history.size() - 1) {
            historyIndex++;
            String text = history.get(historyIndex);
            inputArea.replaceText(text);
        } else {
            inputArea.replaceText("");
            historyIndex = history.size();
        }
    }

    public void dispose() {
        inputArea.dispose();
        outputArea.dispose();
    }
}

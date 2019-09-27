package dev.jfxde.jfxext.control;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.control.editor.Editor;
import dev.jfxde.jfxext.richtextfx.ContextMenuBuilder;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;

public class SplitConsoleView extends BorderPane {

    private ConsoleModel consoleModel;
    private Editor<CodeArea> editor = new Editor<>(new CodeArea());
    private CodeArea inputArea = editor.getArea();
    private CodeArea outputArea = new CodeArea();
    private ObservableList<TextStyleSpans> history = FXCollections.observableArrayList();
    private int historyIndex;

    public SplitConsoleView() {
        this(new ConsoleModel());
    }

    public SplitConsoleView(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
        setGraphics();
        setBehavior();
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public ObservableList<TextStyleSpans> getHistory() {
        return history;
    }

    private ObservableList<TextStyleSpans> getInput() {
        return consoleModel.getInput();
    }

    private ObservableList<TextStyleSpans> getOutput() {
        return consoleModel.getOutput();
    }

    public Editor<CodeArea> getEditor() {
        return editor;
    }

    private void setGraphics() {
        getStylesheets().add(getClass().getResource("console.css").toExternalForm());
        outputArea.setEditable(false);
        outputArea.getStylesheets().add(getClass().getResource("code-area.css").toExternalForm());
        outputArea.setFocusTraversable(false);
        ContextMenuBuilder.get(outputArea).copy().selectAll().clear();

        inputArea.requestFocus();
        inputArea.getStylesheets().add(getClass().getResource("code-area.css").toExternalForm());
        inputArea.setWrapText(true);
        inputArea.getStyleClass().add("jd-input");
        ContextMenuBuilder.get(inputArea).copy().cut().paste().selectAll();

        SplitPane splitPane = new SplitPane(new VirtualizedScrollPane<>(outputArea), new VirtualizedScrollPane<>(inputArea));
        splitPane.setOrientation(Orientation.VERTICAL);
        splitPane.setDividerPositions(0.8f);

        setCenter(splitPane);
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
        TextStyleSpans span = new TextStyleSpans(inputArea.getText(), inputArea.getStyleSpans(0, inputArea.getText().length()));
        getInput().addAll(span, new TextStyleSpans("\n"));

        history.add(span);
        historyIndex = history.size();

        inputArea.clear();
    }

    private void historyUp() {

        if (historyIndex > 0 && historyIndex <= history.size()) {
            historyIndex--;
            TextStyleSpans span = history.get(historyIndex);
            inputArea.replaceText(span.getText());
            inputArea.setStyleSpans(0, span.getStyleSpans());
        }
    }

    private void historyDown() {

        if (historyIndex >= 0 && historyIndex < history.size() - 1) {
            historyIndex++;
            TextStyleSpans span = history.get(historyIndex);
            inputArea.replaceText(span.getText());
            inputArea.setStyleSpans(0, span.getStyleSpans());
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

package dev.jfxde.jfxext.control;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jfxde.jfxext.richtextfx.ContextMenuBuilder;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;

public class ConsoleView extends StackPane {

    private ConsoleModel consoleModel;
    private CodeArea area = new CodeArea();
    private int lastLineBreakPosition;

    public ConsoleView() {
        this(new ConsoleModel());
    }

    public ConsoleView(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
        setGraphics();
        setBehavior();
    }

    private void setGraphics() {
        getStylesheets().add(getClass().getResource("console.css").toExternalForm());

        area.requestFocus();
        area.getStylesheets().add(getClass().getResource("code-area.css").toExternalForm());
        area.setWrapText(true);
        area.getStyleClass().add("jd-input");
        ContextMenuBuilder.get(area).copy().cut().paste().selectAll();

        getChildren().add(new VirtualizedScrollPane<>(area));
    }

    private void setBehavior() {

        area.sceneProperty().addListener((v, o, n) -> {
            if (n != null) {
                area.requestFocus();
            }
        });

        area.addEventFilter(KeyEvent.KEY_PRESSED, e -> {

            if (e.getCode() == KeyCode.ENTER) {
                e.consume();
                enter();
            }
        });

        consoleModel.getOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                    Platform.runLater(() -> {

                        for (TextStyleSpans span : added) {
                            int from = area.getLength();
                            area.appendText(span.getText());
                            area.setStyleSpans(from, span.getStyleSpans());
                        }
                        area.moveTo(area.getLength());
                        area.requestFollowCaret();
                        lastLineBreakPosition = area.getLength();
                    });
                }
            }
        });
    }

    private void enter() {

        String input = area.getText(lastLineBreakPosition, area.getLength());
        area.deleteText(lastLineBreakPosition, area.getLength());

        TextStyleSpans span = new TextStyleSpans(input + "\n");
        consoleModel.getInput().addAll(span);
    }

    public ConsoleModel getConsoleModel() {
        return consoleModel;
    }

    public void dispose() {
        area.dispose();
    }
}

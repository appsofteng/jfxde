package dev.jfxde.jfx.scene.control;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.jfxde.fxmisc.richtext.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public class ConsoleModel {

    public static final String NORMAL_STYLE = "jd-console-normal";
    public static final String COMMENT_STYLE = "jd-console-comment";
    public static final String HELP_STYLE = "jd-console-help";
    public static final String ERROR_STYLE = "jd-console-error";
    private ObservableList<TextStyleSpans> input = FXCollections.observableArrayList();
    private ObservableList<TextStyleSpans> inputToOutput = FXCollections.observableArrayList();
    private ObservableList<TextStyleSpans> output = FXCollections.observableArrayList();
    private PipedOutputStream outPipe = new PipedOutputStream();
    private PrintStream outToInStream = new PrintStream(outPipe, true);
    private InputStream in;
    private PrintStream out = new PrintStream(new ConsoleOutputStream(NORMAL_STYLE), true);
    private PrintStream err = new PrintStream(new ConsoleOutputStream(ERROR_STYLE), true);
    private AtomicBoolean readFromPipe= new AtomicBoolean();

    public ConsoleModel() {

        try {
            in = new ConsoleInputStream(outPipe);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        setBehavior();
    }

    private void setBehavior() {
        input.addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());

                    if (isReadFromPipe()) {
                        added.stream().map(TextStyleSpans::getText).forEach(outToInStream::print);
                    } else {
                        inputToOutput.addAll(added);
                    }

                    output.addAll(added);
                }
            }
        });
    }

    public boolean isReadFromPipe() {
        return readFromPipe.get();
    }

    public AtomicBoolean getReadFromPipe() {
        return readFromPipe;
    }

    public ObservableList<TextStyleSpans> getInput() {
        return input;
    }

    public ObservableList<TextStyleSpans> getInputToOutput() {
        return inputToOutput;
    }

    public ObservableList<TextStyleSpans> getOutput() {
        return output;
    }

    public InputStream getIn() {
        return in;
    }

    public PrintStream getOut() {
        return out;
    }

    public PrintStream getErr() {
        return err;
    }

    public PrintStream getOut(String style) {
        return new PrintStream(new ConsoleOutputStream(style), true);
    }

    public void addNewLineOutput(TextStyleSpans textStyleSpans) {

        if (textStyleSpans.getText().isEmpty()) {
            return;
        }

        if (!output.isEmpty() && !output.get(output.size() - 1).getText().endsWith("\n")) {
            output.add(new TextStyleSpans("\n"));
        }

        if (!textStyleSpans.getText().isBlank()) {
            output.add(textStyleSpans);
        }
    }

    private synchronized void addOutput(TextStyleSpans textStyleSpans) {
        output.add(textStyleSpans);
    }

    private class ConsoleOutputStream extends ByteArrayOutputStream {

        private String style;

        public ConsoleOutputStream(String style) {
            this.style = style;
        }

        @Override
        public synchronized void flush() throws IOException {
            String string = toString();

            if (string.isEmpty()) {
                return;
            }

            // Windows OS
            string = string.replace("\r", "");

            TextStyleSpans textStyleSpans = new TextStyleSpans(string, style);

            addOutput(textStyleSpans);
            reset();
        }
    }

    private class ConsoleInputStream extends PipedInputStream {

        public ConsoleInputStream(PipedOutputStream outPipe) throws IOException {
            super(outPipe);
        }

        @Override
        public int read() throws IOException {
            readFromPipe.set(true);
            int o = super.read();
            readFromPipe.set(false);
            return o;
        }

        @Override
        public synchronized int read(byte[] b, int off, int len) throws IOException {
            int o = super.read(b, off, len);
            readFromPipe.set(false);
            return o;
        }
    }
}

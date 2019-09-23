package dev.jfxde.jfxext.control;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.PrintStream;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConsoleModel {

    private ObservableList<TextStyleSpans> input = FXCollections.observableArrayList();
    private ObservableList<TextStyleSpans> output = FXCollections.observableArrayList();
    private InputStream in;
    private PrintStream out = new PrintStream(new ConsoleOutputStream(), true);
    private PrintStream err = new PrintStream(new ConsoleOutputStream(), true);

    private class ConsoleOutputStream extends ByteArrayOutputStream {

    }
}

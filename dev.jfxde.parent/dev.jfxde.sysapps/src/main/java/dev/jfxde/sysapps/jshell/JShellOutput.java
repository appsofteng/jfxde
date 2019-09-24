package dev.jfxde.sysapps.jshell;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class JShellOutput {

    protected AppContext context;
    protected JShell jshell;
    protected ObservableList<TextStyleSpans> output;
    protected ObservableList<TextStyleSpans> history;

    JShellOutput(AppContext context, JShell jshell, ObservableList<TextStyleSpans> output) {
        this(context, jshell, output, FXCollections.emptyObservableList());
    }

    JShellOutput(AppContext context, JShell jshell, ObservableList<TextStyleSpans> output, ObservableList<TextStyleSpans> history) {
        this.context = context;
        this.jshell = jshell;
        this.output = output;
        this.history = history;
    }

    abstract void output(String input);

}

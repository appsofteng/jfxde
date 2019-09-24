package dev.jfxde.sysapps.jshell;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class JShellOutput {

    protected AppContext context;
    protected JShell jshell;
    protected ConsoleModel consoleModel;
    protected ObservableList<TextStyleSpans> history;

    JShellOutput(AppContext context, JShell jshell, ConsoleModel consoleModel) {
        this(context, jshell, consoleModel, FXCollections.emptyObservableList());
    }

    JShellOutput(AppContext context, JShell jshell, ConsoleModel consoleModel, ObservableList<TextStyleSpans> history) {
        this.context = context;
        this.jshell = jshell;
        this.consoleModel = consoleModel;
        this.history = history;
    }

    abstract void output(String input);

}

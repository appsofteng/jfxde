package dev.jfxde.sysapps.xjshell.commands;

import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class Command {

    private final String name;
    protected final JShell jshell;
    protected final ObservableList<TextStyleSpans> output;
    protected final ObservableList<TextStyleSpans> history;


    public Command(String name, JShell jshell, ObservableList<TextStyleSpans> output) {
        this(name, jshell, output, FXCollections.emptyObservableList());
    }

    public Command(String name, JShell jshell, ObservableList<TextStyleSpans> output, ObservableList<TextStyleSpans> history) {
        this.name = name;
        this.jshell = jshell;
        this.output = output;
        this.history = history;
    }

    public boolean matches(String input) {
        return input.startsWith(name);
    }

    public String getName() {
        return name;
    }

    public abstract void execute(String input);

    @Override
    public String toString() {
        return name;
    }

}

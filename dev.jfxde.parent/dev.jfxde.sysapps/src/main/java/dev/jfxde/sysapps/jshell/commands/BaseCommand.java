package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class BaseCommand  implements Runnable {

    protected AppContext context;
    protected JShell jshell;
    protected ConsoleModel consoleModel;
    protected ObservableList<String> history;


    public BaseCommand() {
    }

    public BaseCommand(AppContext context, JShell jshell, ConsoleModel consoleModel) {
        this(context, jshell, consoleModel, FXCollections.emptyObservableList());
    }

    public BaseCommand(AppContext context, JShell jshell, ConsoleModel consoleModel, ObservableList<String> history) {
        this.context = context;
        this.jshell = jshell;
        this.consoleModel = consoleModel;
        this.history = history;
    }
}

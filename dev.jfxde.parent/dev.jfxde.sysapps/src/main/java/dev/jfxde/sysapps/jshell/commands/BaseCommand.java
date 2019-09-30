package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.JShellContent;
import dev.jfxde.sysapps.jshell.SnippetOutput;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class BaseCommand  implements Runnable {

    protected AppContext context;
    protected JShell jshell;
    protected ConsoleModel consoleModel;
    protected ObservableList<String> history;
    protected SnippetMatch snippetMatch;
    protected SnippetOutput snippetOutput;
    protected JShellContent jshellContent;

    public BaseCommand(CommandOutput commandOutput) {
        this.context = commandOutput.context;
        this.jshell = commandOutput.jshell;
        this.consoleModel = commandOutput.consoleModel;
        this.history = commandOutput.history;
        this.snippetMatch = commandOutput.snippetMatch;
        this.snippetOutput = commandOutput.snippetOutput;
        this.jshellContent = commandOutput.jshellContent;
    }
}

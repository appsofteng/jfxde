package dev.jfxde.sysapps.jshell;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;

public abstract class JShellOutput {

    public JShellContent jshellContent;
    public AppContext context;
    public JShell jshell;
    public ConsoleModel consoleModel;
    public ObservableList<String> history;
    public SnippetOutput snippetOutput;


    JShellOutput(JShellContent jshellContent) {
       this.jshellContent = jshellContent;
       this.context = jshellContent.context;
       this.jshell = jshellContent.jshell;
       this.consoleModel = jshellContent.consoleView.getConsoleModel();
       this.history = jshellContent.consoleView.getHistory();
       this.snippetOutput = jshellContent.snippetOutput;
    }

    abstract void process(String input);

}

package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.CommandOutput;
import dev.jfxde.sysapps.jshell.JShellContent;
import dev.jfxde.sysapps.jshell.SnippetOutput;
import javafx.collections.ObservableList;
import jdk.jshell.JShell;
import picocli.CommandLine.Option;

public abstract class BaseCommand  implements Runnable {

    protected CommandOutput commandOutput;
    protected AppContext context;
    protected JShell jshell;
    protected ConsoleModel consoleModel;
    protected ObservableList<String> history;
    protected SnippetMatch snippetMatch;
    protected SnippetOutput snippetOutput;
    protected JShellContent jshellContent;

    @Option(names = {"-h", "--help"}, usageHelp = true, descriptionKey = "help")
    private boolean usageHelpRequested;

    public BaseCommand(CommandOutput commandOutput) {
        this.commandOutput = commandOutput;
        this.context = commandOutput.context;
        this.jshell = commandOutput.jshell;
        this.consoleModel = commandOutput.consoleModel;
        this.history = commandOutput.history;
        this.snippetMatch = commandOutput.snippetMatch;
        this.snippetOutput = commandOutput.snippetOutput;
        this.jshellContent = commandOutput.jshellContent;
    }
}

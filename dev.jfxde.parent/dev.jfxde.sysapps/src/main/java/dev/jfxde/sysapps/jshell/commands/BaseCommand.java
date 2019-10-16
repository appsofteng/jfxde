package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Model.CommandSpec;
import picocli.CommandLine.Option;
import picocli.CommandLine.Spec;

public abstract class BaseCommand  implements Runnable {

    protected CommandProcessor commandProcessor;

    @Option(names = {"-h", "--help"}, usageHelp = true, descriptionKey = "-h")
    protected boolean help;

    @Spec
    protected CommandSpec commandSpec;

    public BaseCommand(CommandProcessor commandProcessor) {
        this.commandProcessor = commandProcessor;
    }
}

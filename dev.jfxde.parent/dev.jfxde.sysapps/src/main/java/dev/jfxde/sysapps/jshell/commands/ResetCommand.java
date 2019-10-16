package dev.jfxde.sysapps.jshell.commands;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.sysapps.jshell.CommandProcessor;
import picocli.CommandLine.Command;

@Command(name = "/reset")
public class ResetCommand extends BaseCommand {

    public ResetCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        commandProcessor.getSession().getFeedback().normaln(commandProcessor.getSession().getContext().rc().getString("resetingState"), ConsoleModel.COMMENT_STYLE);
        commandProcessor.getSession().reset();
    }
}

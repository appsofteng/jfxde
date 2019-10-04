package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.nio.file.Files;

import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.sysapps.jshell.CommandProcessor;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import picocli.CommandLine.Command;

@Command(name = "/save")
public class SaveCommand extends BaseCommand {

    public SaveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(commandProcessor.getSession().getWindow());

            if (file != null) {
                commandProcessor.getSession().getContext().tc().executeSequentially(TaskUtils.createTask(() -> {
                    try (var f = Files.newBufferedWriter(file.toPath())) {
                        commandProcessor.getSession().getJshell().snippets()
                        .filter(s -> commandProcessor.getSession().getJshell().status(s).isActive())
                        .forEach(s -> {try { f.append(s.source()); f.newLine();} catch (Exception e) { throw new RuntimeException(e);}});
                    }
                }));

            }

        });
    }
}

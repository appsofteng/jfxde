package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.nio.file.Files;

import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.sysapps.jshell.CommandOutput;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import picocli.CommandLine.Command;

@Command(name = "/save")
public class SaveCommand extends BaseCommand {

    public SaveCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {

        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(jshellContent.getScene().getWindow());

            if (file != null) {
                context.tc().executeSequentially(TaskUtils.createTask(() -> {
                    try (var f = Files.newBufferedWriter(file.toPath())) {
                        jshell.snippets()
                        .filter(s -> jshell.status(s).isActive())
                        .forEach(s -> {try { f.append(s.source()); f.newLine();} catch (Exception e) { throw new RuntimeException(e);}});
                    }
                }));

            }

        });
    }
}

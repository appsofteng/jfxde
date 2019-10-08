package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "/open")
public class OpenCommand extends BaseCommand {

    @Option(names = { "default", "DEFAULT"}, descriptionKey = "/open.default")
    private boolean defaultImports;

    @Option(names = { "printing", "PRINTING"},  descriptionKey = "/open.printing")
    private boolean printing;

    public OpenCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (defaultImports) {
            commandProcessor.getSession().loadDefault();
        }

        if (printing) {
            commandProcessor.getSession().loadPrinting();
        }

        if (!defaultImports && !printing) {
            Platform.runLater(() -> {
                FileChooser fileChooser = new FileChooser();
                File file = fileChooser.showOpenDialog(commandProcessor.getSession().getWindow());
                if (file != null) {
                    try {
                        String spippets = Files.readString(file.toPath());
                        commandProcessor.getSession().processAsync(spippets);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
}

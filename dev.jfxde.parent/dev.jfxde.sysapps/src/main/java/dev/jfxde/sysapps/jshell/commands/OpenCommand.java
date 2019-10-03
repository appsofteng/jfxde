package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;

import dev.jfxde.sysapps.jshell.CommandOutput;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/open")
public class OpenCommand extends BaseCommand {

    @Parameters
    private ArrayList<String> parameters;

    public OpenCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showOpenDialog(jshellContent.getScene().getWindow());
            if (file != null) {
                try {
                    String spippets = Files.readString(file.toPath());
                    snippetOutput.process(spippets);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }
}

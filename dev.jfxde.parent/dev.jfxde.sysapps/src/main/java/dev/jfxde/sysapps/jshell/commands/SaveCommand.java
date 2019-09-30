package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.util.ArrayList;

import dev.jfxde.sysapps.jshell.CommandOutput;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

@Command(name = "/save")
public class SaveCommand extends BaseCommand {

    @Parameters
    private ArrayList<String> parameters;

    public SaveCommand(CommandOutput commandOutput) {
        super(commandOutput);
    }

    @Override
    public void run() {

        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(jshellContent.getScene().getWindow());
        });


//        try (var f = Files.newBufferedWriter(Paths, options))
//       jshell.snippets().forEach(s -> );
    }
}

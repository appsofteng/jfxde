package dev.jfxde.sysapps.jshell.commands;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.stream.Stream;

import org.jooq.lambda.Unchecked;

import dev.jfxde.sysapps.jshell.CommandProcessor;
import javafx.application.Platform;
import javafx.stage.FileChooser;
import jdk.jshell.Snippet;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "/save")
public class SaveCommand extends BaseCommand {


    @Parameters(paramLabel = "{name|id|startID-endID}[ {name|id|startID-endID}...]", descriptionKey = "/save.ids")
    private ArrayList<String> parameters;

    @Option(names = "-all", descriptionKey = "/save.-all")
    private boolean all;

    @Option(names = "-start", descriptionKey = "/save.-start")
    private boolean start;

    @Option(names = "-history", descriptionKey = "/save.-history")
    private boolean history;

    public SaveCommand(CommandProcessor commandProcessor) {
        super(commandProcessor);
    }

    @Override
    public void run() {

        if (Stream.of(parameters!= null && !parameters.isEmpty(), all, start, history).filter(o -> o).count() > 1) {
            commandProcessor.getCommandLine().getErr()
                    .println(commandProcessor.getSession().getContext().rc().getString("onlyOneOptionAllowed") + "\n");
            return;
        }

        if (parameters != null && !parameters.isEmpty()) {
            save(commandProcessor.matches(parameters).stream().map(Snippet::source));
        } else if (all) {
            save(commandProcessor.getSession().getJshell().snippets().map(Snippet::source));
        } else if (start) {
               save(commandProcessor.getSession().getJshell().snippets()
                        .filter(s -> Integer.parseInt(s.id()) <= commandProcessor.getSession().getStartSnippetMaxIndex())
                        .map(Snippet::source));
        } else if (history) {
            save(commandProcessor.getSession().getHistory().stream());
        } else {
            save(commandProcessor.getSession().getJshell().snippets()
                        .filter(s -> commandProcessor.getSession().getJshell().status(s).isActive())
                        .map(Snippet::source));
        }
    }

    private void save(Stream<String> snippets) {
        Platform.runLater(() -> {
            FileChooser fileChooser = new FileChooser();
            File file = fileChooser.showSaveDialog(commandProcessor.getSession().getWindow());

            if (file != null) {
                commandProcessor.getSession().getContext().tc().executeSequentially(() -> {
                    try (var f = Files.newBufferedWriter(file.toPath())) {
                        snippets.forEach(Unchecked.consumer(s -> { f.append(s.strip()); f.newLine();}));
                    }
                });
            }
        });
    }
}

package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePointer;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SearchFileDialog extends InternalDialog {

    private AutoCompleteField<String> pathField;
    private AutoCompleteField<String> textField;
    private TreeItem<FilePointer> root;
    private boolean searching;
    private AtomicBoolean stop;

    public SearchFileDialog(Node node, List<FXPath> searchPaths) {
        super(node);

        setTitle(FXResourceBundle.getBundle().getString​("search"));

        pathField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(pathField.promptTextProperty(), "pathWildcards");

        textField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(textField.promptTextProperty(), "textRegex");

        root = new TreeItem<>();
        TreeView<FilePointer> filePointers = new TreeView<>(root);
        filePointers.setPrefHeight(200);
        filePointers.setShowRoot(false);

        Button searchButton = new Button();
        searchButton.disableProperty().bind(Bindings.createBooleanBinding(() -> pathField.getText().isBlank() && textField.getText().isBlank(),
                pathField.textProperty(), textField.textProperty()));

        searchButton.setOnAction(e -> {

            if (searching) {
                stop.set(true);
                FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");
                searching = false;
            } else {
                pathField.store();
                textField.store();
                root.getChildren().clear();
                stop = new AtomicBoolean();
                FXFiles.search(searchPaths, pathField.getText(), textField.getText(), this::found, stop)
                        .thenRun(() -> XPlatform.runFX(() -> {
                            FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");
                            searching = false;
                        }));
                searching = true;
                FXResourceBundle.getBundle().put(searchButton.textProperty(), "stop");
            }
        });

        Button closeButton = new Button();
        closeButton.setOnAction(e -> {
            stop.set(true);
            close();
        });

        FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");

        FXResourceBundle.getBundle().put(closeButton.textProperty(), "close");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(searchButton, closeButton);

        VBox pane = new VBox();
        var margin = new Insets(5);
        VBox.setMargin(pathField, margin);
        VBox.setMargin(textField, margin);
        VBox.setMargin(filePointers, margin);
        VBox.setVgrow(filePointers, Priority.ALWAYS);
        VBox.setMargin(buttonBar, margin);
        pane.getChildren().addAll(pathField, textField, filePointers, buttonBar);

        setContent(pane);
    }

    private void found(FilePointer filePointer) {
        XPlatform.runFX(() -> {
            root.getChildren().add(new TreeItem<>(filePointer));
        });
    }
}

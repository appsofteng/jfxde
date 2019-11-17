package dev.jfxde.sysapps.editor;

import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
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

    public SearchFileDialog(Node node) {
        super(node);
        setTitle(FXResourceBundle.getBundle().getString​("search"));

        AutoCompleteField<String> pathField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(pathField.promptTextProperty(), "pathRegex");

        AutoCompleteField<String> textField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(textField.promptTextProperty(), "textRegex");

        TreeItem<FilePointer> root = new TreeItem<>();
        TreeView<FilePointer> filePointers = new TreeView<>(root);
        filePointers.setPrefHeight(200);
        filePointers.setShowRoot(false);

        Button searchButton = new Button();
        searchButton.disableProperty().bind(Bindings.createBooleanBinding(() -> pathField.getText().isBlank() && textField.getText().isBlank(), pathField.textProperty(), textField.textProperty()));
        searchButton.setOnAction(e -> {
            pathField.store();
            textField.store();
            search();
        });

        Button closeButton = new Button();
        closeButton.setOnAction(e -> close());

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

    private void search() {

    }
}

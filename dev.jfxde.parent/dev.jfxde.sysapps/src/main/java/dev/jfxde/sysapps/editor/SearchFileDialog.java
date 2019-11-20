package dev.jfxde.sysapps.editor;

import java.util.concurrent.atomic.AtomicBoolean;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FilePointer;
import javafx.beans.binding.Bindings;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SearchFileDialog extends InternalDialog {

    private ObservableList<Search> searches;
    private ChoiceBox<Search> searchChoice;
    private Search search;
    private AutoCompleteField<String> pathField;
    private AutoCompleteField<String> textField;
    private Button searchButton = new Button();
    private Button closeButton = new Button();
    private TreeItem<FilePointer> root;
    private boolean searching;
    private AtomicBoolean stop;

    public SearchFileDialog(Node node, ObservableList<Search> searches) {
        super(node);
        this.searches = searches;
        this.search = searches.get(0);

        setGraphics();
        setListeners();
    }

    private void setGraphics() {
        setTitle(FXResourceBundle.getBundle().getString​("search"));

        searchChoice = new ChoiceBox<Search>(searches);
        searchChoice.getSelectionModel().selectFirst();

        pathField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(pathField.promptTextProperty(), "pathWildcards");

        textField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(textField.promptTextProperty(), "textRegex");

        root = new TreeItem<>();
        TreeView<FilePointer> filePointers = new TreeView<>(root);
        filePointers.setPrefHeight(200);
        filePointers.setShowRoot(false);

        searchButton.disableProperty().bind(Bindings.createBooleanBinding(() -> pathField.getText().isBlank() && textField.getText().isBlank(),
                pathField.textProperty(), textField.textProperty()));

        FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");

        FXResourceBundle.getBundle().put(closeButton.textProperty(), "close");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(searchButton, closeButton);

        VBox pane = new VBox();
        var margin = new Insets(5);

        VBox.setMargin(searchChoice, margin);
        VBox.setMargin(pathField, margin);
        VBox.setMargin(textField, margin);
        VBox.setMargin(filePointers, margin);
        VBox.setVgrow(filePointers, Priority.ALWAYS);
        VBox.setMargin(buttonBar, margin);
        pane.getChildren().addAll(searchChoice, pathField, textField, filePointers, buttonBar);

        setContent(pane);
    }

    private void setListeners() {
        searchChoice.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            search = n;
            pathField.setText(n.getPathPattern());
            textField.setText(n.getTextPattern());
            root.getChildren().clear();
            n.getResult().forEach(p -> root.getChildren().add(new TreeItem<>(p)));
        });

        searchButton.setOnAction(e -> {

            if (searching) {
                stop.set(true);
                FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");
                searching = false;
            } else {
                pathField.store();
                textField.store();
                search.setPathPattern(pathField.getText());
                search.setTextPattern(textField.getText());

                root.getChildren().clear();
                stop = new AtomicBoolean();
                FXFiles.search(search.getPaths(), pathField.getText(), textField.getText(), this::found, stop)
                        .thenRun(() -> XPlatform.runFX(() -> {
                            FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");
                            searching = false;
                        }));
                searching = true;
                FXResourceBundle.getBundle().put(searchButton.textProperty(), "stop");
            }
        });

        closeButton.setOnAction(e -> {
            stop.set(true);
            close();
        });
    }

    void update() {
        searchChoice.getSelectionModel().selectFirst();
    }

    private void found(FilePointer filePointer) {
        XPlatform.runFX(() -> {
            root.getChildren().add(new TreeItem<>(filePointer));
            search.getResult().add(filePointer);
        });
    }
}

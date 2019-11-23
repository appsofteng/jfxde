package dev.jfxde.sysapps.editor;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXFiles;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePointer;
import dev.jfxde.logic.data.PathFilePointer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener.Change;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class SearchFileDialog extends InternalDialog {

    private ObservableList<Search> searches;
    private ObjectProperty<Consumer<List<FilePointer>>> fileSelectedHandler;
    private ChoiceBox<Search> searchChoice;
    private Search search;
    private AutoCompleteField<String> pathField;
    private AutoCompleteField<String> textField;
    private CheckBox matchCase = new CheckBox();
    private CheckBox regex = new CheckBox();
    private Button searchButton = new Button();
    private Button closeButton = new Button();
    private TreeItem<FilePointer> root;
    private TreeView<FilePointer> filePointerTree;
    private boolean searching;
    private AtomicBoolean stop = new AtomicBoolean();
    private ListChangeListener<FilePointer> resultListener = (Change<? extends FilePointer> c) -> {
        while (c.next()) {

            if (c.wasAdded()) {
                c.getAddedSubList().forEach(p -> root.getChildren().add(new TreeItem<>(p)));
            } else if (c.wasRemoved()) {
                c.getRemoved().forEach(p -> root.getChildren().removeIf(i -> i.getValue().equals(p)));
            }
        }
    };

    public SearchFileDialog(Node node, ObservableList<Search> searches, ObjectProperty<Consumer<List<FilePointer>>> fileSelectedHandler) {
        super(node);
        this.searches = searches;
        this.search = searches.get(0);
        this.fileSelectedHandler = fileSelectedHandler;

        setGraphics();
        setListeners();
        searchChoice.getSelectionModel().selectFirst();
    }

    private void setGraphics() {
        setTitle(FXResourceBundle.getBundle().getString​("search"));

        searchChoice = new ChoiceBox<>(searches);

        pathField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(pathField.promptTextProperty(), "pathWildcards");

        FXResourceBundle.getBundle().put(matchCase.textProperty(), "matchCase");
        FXResourceBundle.getBundle().put(regex.textProperty(), "regex");
        HBox optionBox = new HBox(5, matchCase, regex);

        textField = new AutoCompleteField<String>();
        FXResourceBundle.getBundle().put(textField.promptTextProperty(), "text");

        root = new TreeItem<>();
        filePointerTree = new TreeView<>(root);
        filePointerTree.setPrefHeight(200);
        filePointerTree.setShowRoot(false);

        searchButton.disableProperty().bind(Bindings.createBooleanBinding(() -> pathField.getText().isBlank() && textField.getText().isBlank(),
                pathField.textProperty(), textField.textProperty())
                .or(Bindings.isEmpty(searches)));

        FXResourceBundle.getBundle().put(searchButton.textProperty(), "search");

        FXResourceBundle.getBundle().put(closeButton.textProperty(), "close");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(searchButton, closeButton);

        VBox pane = new VBox();
        var margin = new Insets(5);

        VBox.setMargin(searchChoice, margin);
        VBox.setMargin(pathField, margin);
        VBox.setMargin(optionBox, margin);
        VBox.setMargin(textField, margin);
        VBox.setMargin(filePointerTree, margin);
        VBox.setVgrow(filePointerTree, Priority.ALWAYS);
        VBox.setMargin(buttonBar, margin);
        pane.getChildren().addAll(searchChoice, pathField, optionBox, textField, filePointerTree, buttonBar);

        setContent(pane);
    }

    private void setListeners() {
        searchChoice.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (o != null) {
                o.getResult().removeListener(resultListener);
            }

            if (n != null) {
                n.getResult().addListener(resultListener);
                search = n;
                pathField.setText(n.getPathPattern());
                textField.setText(n.getTextPattern());
                root.getChildren().clear();
                n.getResult().forEach(p -> root.getChildren().add(new TreeItem<>(p)));
            } else {
                search = null;
                pathField.setText("");
                textField.setText("");
                root.getChildren().clear();
            }
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
                FXFiles.search(search.getPaths(), pathField.getText(), getPattern(), this::found, stop)
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

        filePointerTree.setOnMousePressed(e -> {
            var item = filePointerTree.getSelectionModel().getSelectedItem();

            if (e.getButton() == MouseButton.PRIMARY && item != null) {

                var filePointer = item.getValue();
                if (e.getClickCount() == 2) {
                    getFileSelectedHandler().accept(List.of(filePointer));
                }
            }
        });
    }

    private Pattern getPattern() {

        if (textField.getText().isBlank()) {
            return null;
        }

        int flags = matchCase.isSelected() ? Pattern.CASE_INSENSITIVE : 0;

        if (regex.isSelected()) {
            flags |= Pattern.LITERAL;
        }

        Pattern pattern = Pattern.compile(textField.getText(), flags);

        return pattern;
    }

    private Consumer<List<FilePointer>> getFileSelectedHandler() {
        return fileSelectedHandler.get();
    }

    void update() {
        searchChoice.getSelectionModel().selectFirst();
    }

    private void found(FilePointer filePointer) {
        XPlatform.runFX(() -> {
            search.getResult().add(filePointer);
        });
    }
}

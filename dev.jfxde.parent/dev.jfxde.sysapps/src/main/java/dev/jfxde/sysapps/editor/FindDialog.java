package dev.jfxde.sysapps.editor;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.VBox;

public class FindDialog extends InternalDialog {

    private FindBox findBox = new FindBox();
    private AutoCompleteField<String> replaceField = new AutoCompleteField<String>();

    private Button replaceButton = new Button();
    private Button replaceAllButton = new Button();
    private Button closeButton = new Button();
    private Consumer<String> replace = s -> {};
    private Consumer<String> replaceAll = s -> {};

    public FindDialog(Node node) {
        super(node);

        setTitle(FXResourceBundle.getBundle().getString​("findReplace"));

        setGraphics();
    }

    StringProperty foundCountProperty() {
        return findBox.foundCountProperty();
    }

    FindDialog text(String value) {
        if (value != null && !value.isEmpty()) {
            findBox.setText(value);
        }

        setFocusOwner(findBox);
        return this;
    }

    FindDialog findPrevious(BiConsumer<Pattern, Boolean> consumer) {
        findBox.setFindPrevious(consumer);
        return this;
    }

    FindDialog findNext(BiConsumer<Pattern, Boolean> consumer) {
        findBox.setFindNext(consumer);
        return this;
    }

    FindDialog replace(Consumer<String> consumer) {
        this.replace = consumer;
        return this;
    }

    FindDialog replaceAll(Consumer<String> consumer) {
        this.replaceAll = consumer;
        return this;
    }

    private void setGraphics() {

        FXResourceBundle.getBundle().put(findBox.promptTextProperty(), "find");
        FXResourceBundle.getBundle().put(replaceField.promptTextProperty(), "replace");

        replaceButton.disableProperty().bind(findBox.textProperty().isEmpty());
        replaceButton.setOnAction(e -> replace());
        replaceAllButton.disableProperty().bind(findBox.textProperty().isEmpty());
        replaceAllButton.setOnAction(e -> replaceAll());
        closeButton.setOnAction(e -> close());

        FXResourceBundle.getBundle().put(replaceButton.textProperty(), "replace");
        FXResourceBundle.getBundle().put(replaceAllButton.textProperty(), "replaceAll");
        FXResourceBundle.getBundle().put(closeButton.textProperty(), "close");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(replaceButton, replaceAllButton, closeButton);


        VBox pane = new VBox();
        var margin = new Insets(5);

        VBox.setMargin(findBox, margin);
        VBox.setMargin(replaceField, margin);
        VBox.setMargin(buttonBar, margin);

        pane.getChildren().addAll(findBox, replaceField, buttonBar);

        setContent(pane);
        setFocusOwner(findBox);
    }

    private void replace() {
        replaceField.store();
        findBox.find();
        replace.accept(replaceField.getText());
    }

    private void replaceAll() {
        replaceField.store();
        findBox.find();
        replaceAll.accept(replaceField.getText());
    }
}
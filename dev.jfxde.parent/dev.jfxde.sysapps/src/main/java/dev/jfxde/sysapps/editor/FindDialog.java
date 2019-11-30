package dev.jfxde.sysapps.editor;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
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

    public FindDialog(Node node) {
        super(node);

        setTitle(FXResourceBundle.getBundle().getString​("findReplace"));

        setGraphics();
    }

    FindDialog text(String value) {
        if (value != null && !value.isEmpty()) {
            findBox.setText(value);
        }

        setFocusOwner(findBox);
        return this;
    }

    FindDialog findPrevious(Consumer<Pattern> consumer) {
        findBox.setFindPrevious(consumer);
        return this;
    }

    FindDialog findNext(Consumer<Pattern> consumer) {
        findBox.setFindNext(consumer);
        return this;
    }

    private void setGraphics() {

        FXResourceBundle.getBundle().put(findBox.promptTextProperty(), "find");
        FXResourceBundle.getBundle().put(replaceField.promptTextProperty(), "replace");

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
}
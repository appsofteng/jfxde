package dev.jfxde.sysapps.editor;

import java.util.function.Consumer;
import java.util.regex.Pattern;

import dev.jfxde.fonts.Fonts;
import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class FindBox extends VBox {

    private CheckBox matchCaseCheck = new CheckBox();
    private CheckBox wholeWordCheck = new CheckBox();
    private CheckBox regexCheck = new CheckBox();
    private CheckBox inSelectionCheck = new CheckBox();

    private AutoCompleteField<String> findField = new AutoCompleteField<String>();
    private Button findPreviousButton;
    private Button findNextButton;
    private Label foundCount = new Label();

    private boolean previous;
    private Consumer<Pattern> findPrevious = p -> {};
    private Consumer<Pattern> findNext = p -> {};

    public FindBox() {
        setGraphics();
    }

    private void setGraphics() {

        wholeWordCheck.disableProperty().bind(regexCheck.selectedProperty());
        regexCheck.disableProperty().bind(wholeWordCheck.selectedProperty());

        FXResourceBundle.getBundle().put(matchCaseCheck.textProperty(), "matchCase");
        FXResourceBundle.getBundle().put(wholeWordCheck.textProperty(), "wholeWord");
        FXResourceBundle.getBundle().put(regexCheck.textProperty(), "regex");
        FXResourceBundle.getBundle().put(inSelectionCheck.textProperty(), "inSelection");
        HBox optionBox = new HBox(5, matchCaseCheck, wholeWordCheck, regexCheck, inSelectionCheck);

        findField.setOnCompleted(this::find);

        findPreviousButton = new Button(Fonts.FontAwesome.CHEVRON_UP);
        findPreviousButton.setFocusTraversable(false);
        findPreviousButton.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        findPreviousButton.setMaxHeight(Double.MAX_VALUE);
        findPreviousButton.setMinWidth(USE_PREF_SIZE);
        findPreviousButton.disableProperty().bind(findField.textProperty().isEmpty());
        findPreviousButton.setOnAction(e -> findPrevious());

        findNextButton = new Button(Fonts.FontAwesome.CHEVRON_DOWN);
        findNextButton.setFocusTraversable(false);
        findNextButton.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        findNextButton.setMaxHeight(Double.MAX_VALUE);
        findNextButton.setMinWidth(USE_PREF_SIZE);
        findNextButton.disableProperty().bind(findField.textProperty().isEmpty());
        findNextButton.setOnAction(e -> findNext());

        foundCount.getStyleClass().add("jd-editor-label");
        foundCount.setMaxHeight(Double.MAX_VALUE);
        foundCount.setMinWidth(USE_PREF_SIZE);

        HBox.setHgrow(findField, Priority.ALWAYS);
        HBox fieldBox = new HBox(findField, findPreviousButton, findNextButton, foundCount);

        var margin = new Insets(0,0,5,0);

        VBox.setMargin(optionBox, margin);

        getChildren().addAll(optionBox, fieldBox);
    }

    void setText(String value) {
        findField.setText(value);
    }

    public StringProperty promptTextProperty() {
        return findField.promptTextProperty();
    }

    public void setFindPrevious(Consumer<Pattern> findPrevious) {
        this.findPrevious = findPrevious;
    }

    public void setFindNext(Consumer<Pattern> findNext) {
        this.findNext = findNext;
    }

    private void find(String value) {
        if (previous) {
            findPrevious.accept(getPattern());
        } else {
            findNext.accept(getPattern());
        }

        findField.requestFocus();
    }

    private void findPrevious() {
        previous = true;
        findField.store();
        findPrevious.accept(getPattern());
    }

    private void findNext() {
        previous = false;
        findField.store();
        findNext.accept(getPattern());
    }

    private Pattern getPattern() {

        String regex = findField.getText();

        if (regex.isEmpty()) {
            return null;
        }

        int flags = matchCaseCheck.isSelected() ? 0 : Pattern.CASE_INSENSITIVE;

        if (wholeWordCheck.isSelected()) {
            regex = String.format("\\b%s\\b", regex);
        } else if (!regexCheck.isDisabled() && !regexCheck.isSelected()) {
            flags |= Pattern.LITERAL;
        }

        Pattern pattern = Pattern.compile(regex, flags);

        return pattern;
    }

    @Override
    public void requestFocus() {
        super.requestFocus();
        findField.requestFocus();
    }
}

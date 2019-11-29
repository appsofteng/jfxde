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
    private Button findPrevious;
    private Button findNext;
    private Label foundCount = new Label();

    private Consumer<Pattern> findConsumer = p -> {};

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

        findPrevious = new Button(Fonts.FontAwesome.CHEVRON_UP);
        findPrevious.setFocusTraversable(false);
        findPrevious.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        findPrevious.setMaxHeight(Double.MAX_VALUE);
        findPrevious.setMinWidth(USE_PREF_SIZE);
        findPrevious.disableProperty().bind(findField.textProperty().isEmpty());

        findNext = new Button(Fonts.FontAwesome.CHEVRON_DOWN);
        findNext.setFocusTraversable(false);
        findNext.getStyleClass().addAll("jd-font-awesome-solid", "jd-editor-button");
        findNext.setMaxHeight(Double.MAX_VALUE);
        findNext.setMinWidth(USE_PREF_SIZE);
        findNext.disableProperty().bind(findField.textProperty().isEmpty());

        foundCount.getStyleClass().add("jd-editor-label");
        foundCount.setMaxHeight(Double.MAX_VALUE);
        foundCount.setMinWidth(USE_PREF_SIZE);

        HBox.setHgrow(findField, Priority.ALWAYS);
        HBox fieldBox = new HBox(findField, findPrevious, findNext, foundCount);

        var margin = new Insets(0,0,5,0);

        VBox.setMargin(optionBox, margin);

        getChildren().addAll(optionBox, fieldBox);
    }

    public StringProperty promptTextProperty() {
        return findField.promptTextProperty();
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
}

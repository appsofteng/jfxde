package dev.jfxde.jfx.scene.control;

import java.util.Collection;
import java.util.TreeSet;
import java.util.function.Consumer;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javafx.application.Platform;
import javafx.beans.property.StringProperty;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.TextField;
import javafx.scene.layout.Region;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class AutoCompleteField<T> extends Region {

    private Collection<T> suggestions;
    private TextField textField;
    private AutoCompletionBinding<T> binding;
    private StringConverter<T> converter = (StringConverter<T>) new DefaultStringConverter();
    private Consumer<T> onCompleted;
    private Consumer<T> onAction;;

    public AutoCompleteField() {
        this("");
    }

    public AutoCompleteField(String text) {
        this(text, new TreeSet<>());
    }

    public AutoCompleteField(Collection<T> suggestions) {
        this("", suggestions);
    }

    public AutoCompleteField(String text, Collection<T> suggestions) {
        this.suggestions = suggestions;
        textField = TextFields.createClearableTextField();
        textField.setText(text);
        Platform.runLater(this::bindAutoCompletion);
        textField.setOnAction(e -> onSelected());

        textField.focusedProperty().addListener((v, o, n) -> {
            if (n) {
                Platform.runLater(() -> textField.deselect());
            }
        });

        getChildren().add(textField);
    }

    private void bindAutoCompletion() {
        binding = TextFields.bindAutoCompletion(textField, suggestions);
        binding.setOnAutoCompleted(e -> onCompleted());
        binding.prefWidthProperty().bind(textField.widthProperty());
    }

    private void onSelected() {

        if (store()) {
            if (onAction != null) {
                onAction.accept(converter.fromString(textField.getText()));
            } else {
                onCompleted();
            }
        }
    }

    public boolean store() {
        if (textField.getText().isBlank()) {
            return false;
        }

        suggestions.add(converter.fromString(textField.getText()));

        if (binding != null) {
            binding.dispose();
        }

        bindAutoCompletion();

        return true;
    }

    public void setOnAction(Consumer<T> onAction) {
        this.onAction = onAction;
    }

    public void setOnCompleted(Consumer<T> consumer) {
        this.onCompleted = consumer;
    }

    private void onCompleted() {
        if (onCompleted != null) {
            onCompleted.accept(converter.fromString(textField.getText()));
        }
    }

    public StringProperty textProperty() {
        return textField.textProperty();
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String value) {
        textField.setText(value);
    }

    public StringProperty promptTextProperty() {
        return textField.promptTextProperty();
    }

    @Override
    public void requestFocus() {
        textField.requestFocus();
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(textField, 0, 0, getWidth(), getHeight(), 0, new Insets(0), HPos.CENTER, VPos.CENTER);
    }
}

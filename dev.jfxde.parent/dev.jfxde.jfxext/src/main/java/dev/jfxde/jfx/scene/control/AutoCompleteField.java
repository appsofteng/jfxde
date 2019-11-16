package dev.jfxde.jfx.scene.control;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javafx.application.Platform;
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
    private Consumer<T> consumer;

    public AutoCompleteField() {
        this(new HashSet<>());
    }

    public AutoCompleteField(Collection<T> suggestions) {
        this.suggestions = suggestions;
        textField = TextFields.createClearableTextField();
        Platform.runLater(this::bindAutoCompletion);
        textField.setOnAction(e -> onSelected());
        getChildren().add(textField);
    }

    private void bindAutoCompletion() {
        binding = TextFields.bindAutoCompletion(textField, suggestions);
        binding.setOnAutoCompleted(e -> onCompleted());
        binding.prefWidthProperty().bind(textField.widthProperty());
    }

    private void onSelected() {

        if (textField.getText().isBlank()) {
            return;
        }

        suggestions.add(converter.fromString(textField.getText()));
        if (binding != null) {
            binding.dispose();
        }

        bindAutoCompletion();

        onCompleted();
    }

    public void setOnCompleted(Consumer<T> consumer) {
        this.consumer = consumer;
    }

    private void onCompleted() {
        if (consumer != null) {
            consumer.accept(converter.fromString(textField.getText()));
        }
    }

    public String getText() {
        return textField.getText();
    }

    public void setText(String value) {
        textField.setText(value);
    }

    @Override
    protected void layoutChildren() {
        layoutInArea(textField, 0, 0, getWidth(), getHeight(), 0, new Insets(0), HPos.CENTER, VPos.CENTER);
    }
}

package dev.jfxde.sysapps.editor;

import java.util.function.Consumer;

import org.controlsfx.control.textfield.TextFields;
import org.controlsfx.validation.Severity;
import org.controlsfx.validation.ValidationSupport;
import org.controlsfx.validation.Validator;

import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;

public class GoToDialog extends InternalDialog {

    private TextField field;
    private Consumer<Integer> onGo;
    private ValidationSupport validationSupport = new ValidationSupport();

    public GoToDialog(Node node) {
        super(node, Modality.WINDOW_MODAL);

        setTitle(FXResourceBundle.getBundle().getString​("goToLine"));
        setUseComputedSize();

        setGraphics();
    }

    private void setGraphics() {
        field = TextFields.createClearableTextField();
        field.setOnAction(e -> onGo());
        validationSupport.registerValidator(field, Validator.createRegexValidator(FXResourceBundle.getBundle().getString​("numberRequired"), "\\d+", Severity.ERROR));

        Button okBtn = new Button();
        okBtn.disableProperty().bind(field.textProperty().isEmpty().or(validationSupport.invalidProperty()));
        okBtn.setOnAction(e -> onGo());
        Button cancelBtn = new Button();
        cancelBtn.setOnAction(e -> close());
        FXResourceBundle.getBundle().put(okBtn.textProperty(), "ok");
        FXResourceBundle.getBundle().put(cancelBtn.textProperty(), "cancel");

        ButtonBar buttonBar = new ButtonBar();
        buttonBar.getButtons().addAll(okBtn, cancelBtn);

        var margin = new Insets(5);
        VBox box = new VBox(field, buttonBar);
        VBox.setMargin(field, margin);
        VBox.setMargin(buttonBar, margin);

        setContent(box);

        setFocusOwner(field);
    }

    private void onGo() {
        if (onGo != null) {
            onGo.accept(Integer.parseInt(field.getText()));
        }
        close();
    }

    public GoToDialog setOnGo(Consumer<Integer> onGo) {
        this.onGo = onGo;

        return this;
    }
}

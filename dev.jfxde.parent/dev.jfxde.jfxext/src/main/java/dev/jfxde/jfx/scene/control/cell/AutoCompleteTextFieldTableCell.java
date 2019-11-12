package dev.jfxde.jfx.scene.control.cell;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class AutoCompleteTextFieldTableCell<S,T> extends TextFieldTableCell<S, T>{

    private AutoCompletionBinding<T> fieldBinding;
    private ObservableList<T> suggestions;

    public AutoCompleteTextFieldTableCell(StringConverter<T> converter, ObservableList<T> suggestions) {
        super(converter);
       this.suggestions = suggestions;
    }

    public static <S,T> Callback<TableColumn<S,T>, TableCell<S,T>> forTableColumn(
            final StringConverter<T> converter, ObservableList<T> suggestions) {
        return list -> new AutoCompleteTextFieldTableCell<S,T>(converter,suggestions);
    }

    public void startEdit() {
        super.startEdit();

        if (getGraphic() instanceof TextField) {
            var field = (TextField) getGraphic();
            if (fieldBinding != null) {
                fieldBinding.dispose();
            }
            fieldBinding = TextFields.bindAutoCompletion(field, suggestions);
        }
    }
}

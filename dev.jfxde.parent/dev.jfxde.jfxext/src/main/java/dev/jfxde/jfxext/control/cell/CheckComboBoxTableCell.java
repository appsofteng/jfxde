package dev.jfxde.jfxext.control.cell;

import java.util.Collection;

import org.controlsfx.control.CheckComboBox;

import javafx.collections.ObservableList;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;

public class CheckComboBoxTableCell<S, T> extends TableCell<S, Collection<T>> {

    private StringConverter<Collection<T>> converter;
    private CheckComboBox<T> comboBox;
    private ObservableList<T> items;

    public static <S,T> Callback<TableColumn<S,Collection<T>>, TableCell<S,Collection<T>>> forTableColumn(
            final StringConverter<Collection<T>> converter, ObservableList<T> items) {
        return list -> new CheckComboBoxTableCell<>(converter, items);
    }

    public CheckComboBoxTableCell(StringConverter<Collection<T>> converter, ObservableList<T> items) {
        this.converter = converter;
        this.items = items;
    }

    @Override
    public void cancelEdit() {
        if (comboBox != null) {
            commitEdit(comboBox.getCheckModel().getCheckedItems());
        }
    }

    @Override
    public void startEdit() {
        if (!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
            return;
        }

        if (comboBox == null) {
            comboBox = new CheckComboBox<>(items);
        }

        comboBox.getCheckModel().clearChecks();
        getItem().forEach(i -> comboBox.getCheckModel().check(i));

        super.startEdit();
        setText(null);
        setGraphic(comboBox);
    }

    @Override
    protected void updateItem(Collection<T> item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null || item.isEmpty()) {
            setText(null);
            setGraphic(null);
        } else {
            setText(converter.toString(item));
            setGraphic(null);
        }
    }
}

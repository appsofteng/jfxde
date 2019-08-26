package dev.jfxde.ui;

import java.util.function.Consumer;

import javafx.beans.value.ObservableValue;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;

public class HyperlinkTableCell<S> extends TableCell<S, String> {

    private final Hyperlink hyperlink;
    private ObservableValue<String> observable;
    private Consumer<String> linkHandler;

    public HyperlinkTableCell(Consumer<String> linkHandler) {
        this.hyperlink = new Hyperlink();
        this.hyperlink.setMaxWidth(Double.MAX_VALUE);
        this.linkHandler = linkHandler;
    }

    public static <S> Callback<TableColumn<S,String>, TableCell<S,String>> forTableColumn(Consumer<String> linkHandler) {
        return param -> new HyperlinkTableCell<S>(linkHandler);
    }

    @Override
    protected void updateItem(String item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item.isEmpty()) {
            setGraphic(null);
        } else {
            hyperlink.textProperty().unbind();

            final TableColumn<S,String> column = getTableColumn();
            observable = column == null ? null : column.getCellObservableValue(getIndex());

            if (observable != null) {
                hyperlink.textProperty().bind(observable);
            } else if (item != null) {
                hyperlink.setText(item);
            }

            hyperlink.setOnAction(e -> linkHandler.accept(item));

            setGraphic(hyperlink);
        }
    }
}

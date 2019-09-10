package dev.jfxde.sysapps.settings;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ChoiceBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.StackPane;

public class SettingsContent extends StackPane {

    private TableView<PropertyDescriptor> settingTable = new TableView<>();

    @SuppressWarnings("unchecked")
    public SettingsContent(AppContext context) {

        TableColumn<PropertyDescriptor, String> keyColumn = new TableColumn<>();
        keyColumn.textProperty().bind(context.rc().getTextBinding("key"));
        keyColumn.setCellValueFactory(new PropertyValueFactory<>("key"));

        TableColumn<PropertyDescriptor, String> valueColumn = new TableColumn<>();
        valueColumn.textProperty().bind(context.rc().getTextBinding("value"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<>("value"));
        valueColumn.setMaxWidth(Double.MAX_VALUE);
        valueColumn.setCellFactory(c -> new ChoiceBoxTableCell<>(Sys.am().getLocales()));

        settingTable.getColumns().addAll(keyColumn, valueColumn);
        settingTable.getStyleClass().add("jd-table-view");
        settingTable.setEditable(true);

        settingTable.setItems(Sys.sm().getSettings());

        getChildren().add(settingTable);
    }

}

package dev.jfxde.sysapps.settings;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.application.Platform;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.StackPane;

public class SettingsContent extends StackPane {

    @SuppressWarnings("unchecked")
    public SettingsContent(AppContext context) {

        // Although not attached to the scene it throws a null pointer exception
        // sometimes. When run later
        // the exception does not appear.
        Platform.runLater(() -> {
            LazyTreeItem<PropertyDescriptor> root = new LazyTreeItem<>(new PropertyDescriptor(""), i -> Sys.sm().getSubsettings(i));
            TreeTableView<PropertyDescriptor> table = new TreeTableView<>(root);
            table.setShowRoot(false);

            TreeTableColumn<PropertyDescriptor, String> keyColumn = new TreeTableColumn<>();
            keyColumn.textProperty().bind(context.rc().getStringBinding("key"));
            keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("label"));
            keyColumn.setPrefWidth(150);

            TreeTableColumn<PropertyDescriptor, String> valueColumn = new TreeTableColumn<>();
            valueColumn.textProperty().bind(context.rc().getStringBinding("value"));
            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            valueColumn.setMaxWidth(Double.MAX_VALUE);
            valueColumn.setCellFactory(c -> new SettingTableCell());

            table.getColumns().addAll(keyColumn, valueColumn);
            table.getStyleClass().add("jd-table-view");
            table.setEditable(true);

            getChildren().add(table);

        });
    }
}

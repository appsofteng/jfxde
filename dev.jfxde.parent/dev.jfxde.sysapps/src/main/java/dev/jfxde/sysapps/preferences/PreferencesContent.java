package dev.jfxde.sysapps.preferences;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.Preference;
import javafx.application.Platform;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.layout.StackPane;

public class PreferencesContent extends StackPane {

    @SuppressWarnings("unchecked")
    public PreferencesContent(AppContext context) {

        // Although not attached to the scene it throws a null pointer exception
        // sometimes. When run later
        // the exception does not appear.
        Platform.runLater(() -> {
            LazyTreeItem<Preference> root = new LazyTreeItem<>(new Preference())
                    .leaf(i -> i.getValue().isLeaf())
                    .childrenGetter(i -> Sys.pm().getPreferences(i.getValue(), p -> new LazyTreeItem<>(p, i), c -> i.add(c)));
            TreeTableView<Preference> table = new TreeTableView<>(root);
            table.setShowRoot(false);

            TreeTableColumn<Preference, String> keyColumn = new TreeTableColumn<>();
            keyColumn.textProperty().bind(context.rc().getStringBinding("key"));
            keyColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("key"));
            keyColumn.setPrefWidth(150);

            TreeTableColumn<Preference, String> valueColumn = new TreeTableColumn<>();
            valueColumn.textProperty().bind(context.rc().getStringBinding("value"));
            valueColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("value"));
            valueColumn.setMaxWidth(Double.MAX_VALUE);
            valueColumn.setCellFactory(c -> new PreferencesTableCell());

            table.getColumns().addAll(keyColumn, valueColumn);
            table.getStyleClass().add("jd-table-view");
            table.setEditable(true);

            getChildren().add(table);

        });
    }
}

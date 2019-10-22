package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.PermissionDescriptor;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public final class DataUtils {

    private DataUtils() {

    }

    @SuppressWarnings("unchecked")
    static TableView<PermissionDescriptor> getAppPermissionTable(AppProviderDescriptor descriptor) {
        TableView<PermissionDescriptor> table = new TableView<>();

        TableColumn<PermissionDescriptor, String> typeColumn = new TableColumn<>();
        typeColumn.textProperty().bind(Sys.rm().getStringBinding("type"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<PermissionDescriptor, String> targetColumn = new TableColumn<>();
        targetColumn.textProperty().bind(Sys.rm().getStringBinding("target"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));

        TableColumn<PermissionDescriptor, String> actionsColumn = new TableColumn<>();
        actionsColumn.textProperty().bind(Sys.rm().getStringBinding("actions"));
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));

        table.getColumns().addAll(typeColumn, targetColumn, actionsColumn);

        table.itemsProperty().setValue(descriptor.getPermissionDescriptors());

        table.setPrefHeight(200);

        return table;
    }
}

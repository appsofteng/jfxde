package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.PermissionDescriptor;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;

public final class DialogDisplayer {

    private DialogDisplayer() {

    }

    public static void start(Pane owner, AppProviderDescriptor descriptor) {

        Alert alert = new Alert(AlertType.CONFIRMATION);
        var dialogPane = alert.getDialogPane();
        alert.setTitle(Sys.rm().getString("confirmation"));
        alert.setHeaderText(Sys.rm().getString("appPermissions", descriptor.getName()));
        alert.setContentText(Sys.rm().getString("appPermissionConfirmation"));
        dialogPane.setExpandableContent(getAppPermissionTable(descriptor));


        var dialog = new InternalDialog(owner);
        dialog.setResizable(false);
        double w = Math.max(dialogPane.minWidth(-1), dialogPane.getWidth());

        //This is necessary because the dialog pane layout children method has an error.
        dialogPane.heightProperty().addListener((v,o,n) -> {
            dialogPane.setMinHeight(dialogPane.prefHeight(w));
        });
        dialogPane.expandedProperty().addListener((v,n,o) -> {
            dialogPane.setMinHeight(dialogPane.prefHeight(w));
        });


        dialog.setContent(dialogPane).show();

        final Button ok = (Button) alert.getDialogPane().lookupButton(ButtonType.OK);
        ok.setOnAction(e -> {
            dialog.close();
        });

        final Button cancel = (Button) alert.getDialogPane().lookupButton(ButtonType.CANCEL);
        cancel.setOnAction(e -> {
            dialog.close();
        });
//
//		alert.showAndWait().filter(response -> response == ButtonType.OK)
//				.ifPresent(response -> Sys.am().allowAndStart(descriptor));
    }

    @SuppressWarnings("unchecked")
    private static TableView<PermissionDescriptor> getAppPermissionTable(AppProviderDescriptor descriptor) {
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

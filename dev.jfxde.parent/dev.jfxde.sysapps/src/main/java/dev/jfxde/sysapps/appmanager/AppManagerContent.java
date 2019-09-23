package dev.jfxde.sysapps.appmanager;

import java.util.List;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.PermissionDescriptor;
import dev.jfxde.logic.data.TaskDescriptor;
import dev.jfxde.ui.HyperlinkTableCell;
import dev.jfxde.ui.LayoutUtils;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;
import javafx.util.Callback;

public class AppManagerContent extends TabPane {

    private final App app;
    private final AppContext context;
    private TableView<AppProviderDescriptor> appProviderDescriptorTable;

    public AppManagerContent(App app, AppContext appContext) {

    	this.app = app;
        this.context = appContext;

        Tab registeredAppTab = createAppTab();
        Tab permissionTab = createPermissionTab();
        Tab startedAppTab = createStartedAppTab();
        Tab taskTab = createTaskTab();

        getTabs().addAll(registeredAppTab, permissionTab, startedAppTab, taskTab);
    }

    @SuppressWarnings("unchecked")
    private Tab createAppTab() {
        Tab tab = new Tab();
        tab.textProperty().bind(context.rc().getStringBinding("apps"));
        tab.setClosable(false);

        appProviderDescriptorTable = new TableView<>();
        tab.setContent(appProviderDescriptorTable);
        appProviderDescriptorTable.setEditable(true);

        appProviderDescriptorTable.setItems(Sys.am().getAppProviderDescriptors());

        TableColumn<AppProviderDescriptor, Label> nameColumn = new TableColumn<>();
        nameColumn.textProperty().bind(context.rc().getStringBinding("name"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("iconName"));

        TableColumn<AppProviderDescriptor, String> fqnColumn = new TableColumn<>();
        fqnColumn.textProperty().bind(context.rc().getStringBinding("fqn"));
        fqnColumn.setCellValueFactory(new PropertyValueFactory<>("fqn"));

        TableColumn<AppProviderDescriptor, String> versionColumn = new TableColumn<>();
        versionColumn.textProperty().bind(context.rc().getStringBinding("version"));
        versionColumn.setCellValueFactory(new PropertyValueFactory<>("version"));

        TableColumn<AppProviderDescriptor, String> vendorColumn = new TableColumn<>();
        vendorColumn.textProperty().bind(context.rc().getStringBinding("vendor"));
        vendorColumn.setCellValueFactory(new PropertyValueFactory<>("vendor"));

        TableColumn<AppProviderDescriptor, String> websiteColumn = new TableColumn<>();
        websiteColumn.textProperty().bind(context.rc().getStringBinding("website"));
        websiteColumn.setCellValueFactory(new PropertyValueFactory<>("website"));
        websiteColumn.setCellFactory(HyperlinkTableCell.forTableColumn(url -> context.ac().start(url)));

        TableColumn<AppProviderDescriptor, Boolean> allowedColumn = new TableColumn<>();
        allowedColumn.textProperty().bind(context.rc().getStringBinding("allowed"));
        allowedColumn.setCellValueFactory(new PropertyValueFactory<>("allowed"));
        allowedColumn.setCellFactory(c -> {
            CheckBoxTableCell<AppProviderDescriptor, Boolean> cb = new CheckBoxTableCell<>();

            cb.disableProperty().bind(
                    Bindings.createBooleanBinding(() -> cb.getTableRow() != null && cb.getTableRow().getItem() != null ? cb.getTableRow().getItem().isSystem() : true,
                            cb.tableRowProperty()));

            return cb;
        });

        appProviderDescriptorTable.getColumns().addAll(nameColumn, fqnColumn, versionColumn, vendorColumn, websiteColumn, allowedColumn);

        return tab;
    }

    @SuppressWarnings("unchecked")
    private Tab createPermissionTab() {
        Tab tab = new Tab();
        tab.textProperty().bind(context.rc().getStringBinding("permissions"));
        tab.setClosable(false);

        TableView<PermissionDescriptor> table = new TableView<>();
        tab.setContent(table);

        TableColumn<PermissionDescriptor, String> typeColumn = new TableColumn<>();
        typeColumn.textProperty().bind(context.rc().getStringBinding("type"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));

        TableColumn<PermissionDescriptor, String> targetColumn = new TableColumn<>();
        targetColumn.textProperty().bind(context.rc().getStringBinding("target"));
        targetColumn.setCellValueFactory(new PropertyValueFactory<>("target"));

        TableColumn<PermissionDescriptor, String> actionsColumn = new TableColumn<>();
        actionsColumn.textProperty().bind(context.rc().getStringBinding("actions"));
        actionsColumn.setCellValueFactory(new PropertyValueFactory<>("actions"));

        table.getColumns().addAll(typeColumn, targetColumn, actionsColumn);

        table.itemsProperty().bind(Bindings.createObjectBinding(() -> appProviderDescriptorTable.getSelectionModel().getSelectedItem() != null ? appProviderDescriptorTable.getSelectionModel().getSelectedItem().getPermissionDescriptors() : FXCollections.emptyObservableList(),
                appProviderDescriptorTable.getSelectionModel().selectedItemProperty()));

        return tab;
    }

    @SuppressWarnings("unchecked")
    private Tab createStartedAppTab() {
        Tab tab = new Tab();
        tab.textProperty().bind(context.rc().getStringBinding("started"));
        tab.setClosable(false);

        TableView<AppDescriptor> table = new TableView<>();
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<AppDescriptor, String> nameColumn = new TableColumn<>();
        nameColumn.textProperty().bind(context.rc().getStringBinding("name"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<AppDescriptor, String> titleColumn = new TableColumn<>();
        titleColumn.textProperty().bind(context.rc().getStringBinding("title"));
        titleColumn.setCellValueFactory(new PropertyValueFactory<>("display"));

        TableColumn<AppDescriptor, Long> desktopColumn = new TableColumn<>();
        desktopColumn.textProperty().bind(context.rc().getStringBinding("desktop"));
        desktopColumn.setCellValueFactory(c -> c.getValue().getWindow().getDesktop().idProperty().asObject());

        table.getColumns().addAll(nameColumn, titleColumn, desktopColumn);

        table.setItems(Sys.am().getAppDescriptors());

        Button activate = new Button();
        activate.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        activate.textProperty().bind(context.rc().getStringBinding("activate"));
        activate.disableProperty().bind(Bindings.createBooleanBinding(() -> table.getSelectionModel().getSelectedItems().size() != 1
                || table.getSelectionModel().getSelectedItem().getApp() == app, table.getSelectionModel().getSelectedItems()));

        activate.setOnAction(e -> {
            AppDescriptor selectedAppDescriptor = table.getSelectionModel().getSelectedItem();
            selectedAppDescriptor.getWindow().activate();
            Sys.dm().setActiveDesktop(selectedAppDescriptor.getWindow().getDesktop());
        });

        Button stop = new Button();
        stop.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        stop.textProperty().bind(context.rc().getStringBinding("stop"));
        stop.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));

        stop.setOnAction(e -> {
            ObservableList<AppDescriptor> selectedItems = FXCollections.observableArrayList(table.getSelectionModel().getSelectedItems());
            Sys.am().stopAll(selectedItems);
        });

        TilePane buttonPane = LayoutUtils.createTilePane(List.of(activate, stop));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(table);
        borderPane.setBottom(buttonPane);

        tab.setContent(borderPane);

        return tab;
    }

	@SuppressWarnings("unchecked")
	private Tab createTaskTab() {
		Tab tab = new Tab();
		tab.textProperty().bind(context.rc().getStringBinding("tasks"));
		tab.setClosable(false);

		TableView<TaskDescriptor<?>> table = new TableView<>();
		table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		TableColumn<TaskDescriptor<?>, String> appColumn = new TableColumn<>();
		appColumn.textProperty().bind(context.rc().getStringBinding("app"));
		appColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

		TableColumn<TaskDescriptor<?>, String> titleColumn = new TableColumn<>();
		titleColumn.textProperty().bind(context.rc().getStringBinding("title"));
		titleColumn.setCellValueFactory(new PropertyValueFactory<>("title"));
		addCellToolip(titleColumn);

		TableColumn<TaskDescriptor<?>, Double> progressColumn = new TableColumn<>();
		progressColumn.textProperty().bind(context.rc().getStringBinding("progress"));
		progressColumn.setCellValueFactory(c -> c.getValue().getTask().progressProperty().asObject());
		progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());

		TableColumn<TaskDescriptor<?>, String> stateColumn = new TableColumn<>();
		stateColumn.textProperty().bind(context.rc().getStringBinding("state"));
		stateColumn.setCellValueFactory(c -> context.rc().getStringBinding(c.getValue().getTask().stateProperty()));

		table.getColumns().addAll(appColumn, titleColumn, progressColumn, stateColumn);

		table.setItems(Sys.tm().getTaskDescriptors());

		Button cancel = new Button();
		cancel.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		cancel.textProperty().bind(context.rc().getStringBinding("cancel"));
		cancel.disableProperty().bind(Bindings.createBooleanBinding(() -> {

			return table.getSelectionModel().getSelectedItems().size() != 1
					|| !table.getSelectionModel().getSelectedItem().getTask().isRunning();
		}, table.getSelectionModel().getSelectedItems()));

		cancel.setOnAction(e -> {
			ObservableList<TaskDescriptor<?>> selectedItems = table.getSelectionModel().getSelectedItems();
			selectedItems.forEach(TaskDescriptor::cancel);
		});

		Button remove = new Button();
		remove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		remove.textProperty().bind(context.rc().getStringBinding("remove"));
		remove.disableProperty().bind(Bindings.isEmpty(table.getSelectionModel().getSelectedItems()));

		remove.setOnAction(e -> {
			ObservableList<TaskDescriptor<?>> selectedItems = FXCollections
					.observableArrayList(table.getSelectionModel().getSelectedItems());
			Sys.tm().removeAll(selectedItems);
		});

		TilePane buttonPane = LayoutUtils.createTilePane(List.of(cancel, remove));

		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(table);
		borderPane.setBottom(buttonPane);

		tab.setContent(borderPane);

		return tab;
	}

    private <T> void addCellToolip(TableColumn<T, String> column) {
        Callback<TableColumn<T, String>, TableCell<T, String>> cellFactory = column.getCellFactory();

        column.setCellFactory(c -> {
            TableCell<T, String> cell = cellFactory.call(c);
            Tooltip tooltip = new Tooltip();
            tooltip.textProperty().bind(cell.itemProperty());
            cell.tooltipProperty().bind(Bindings.when(
                    cell.emptyProperty().or(cell.itemProperty().isNull()))
                    .then((Tooltip) null)
                    .otherwise(tooltip));

            return cell;
        });
    }

}

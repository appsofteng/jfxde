package dev.jfxde.sysapps.exceptionlog;

import java.time.LocalDateTime;
import java.util.List;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfx.scene.layout.LayoutUtils;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.ExceptionDescriptor;
import javafx.beans.binding.Bindings;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.scene.control.Button;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

public class ExceptionLogContent extends TabPane {

    private TableView<ExceptionDescriptor> exceptionTable;
    private AppContext sysContext;

    public ExceptionLogContent(AppContext sysContext) {

    	this.sysContext = sysContext;

        Tab exceptionTab = createExceptionTab();
        Tab stackTraceTab = createStackTraceTab();

        getTabs().addAll(exceptionTab, stackTraceTab);
    }

    @SuppressWarnings("unchecked")
    private Tab createExceptionTab() {
        Tab tab = new Tab();
        FXResourceBundle.getBundle().put(tab.textProperty(), "exceptions");
        tab.setClosable(false);

        exceptionTable = new TableView<>();

        exceptionTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        TableColumn<ExceptionDescriptor, String> nameColumn = new TableColumn<>();
        FXResourceBundle.getBundle().put(nameColumn.textProperty(), "name");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));

        TableColumn<ExceptionDescriptor, String> messageColumn = new TableColumn<>();
        FXResourceBundle.getBundle().put(messageColumn.textProperty(), "message");
        messageColumn.setCellValueFactory(new PropertyValueFactory<>("message"));

        TableColumn<ExceptionDescriptor, LocalDateTime> timestampColumn = new TableColumn<>();
        FXResourceBundle.getBundle().put(timestampColumn.textProperty(), "timestamp");
        timestampColumn.setCellValueFactory(new PropertyValueFactory<>("timestamp"));

        TableColumn<ExceptionDescriptor, LocalDateTime> causeColumn = new TableColumn<>();
        FXResourceBundle.getBundle().put(causeColumn.textProperty(), "cause");
        causeColumn.setCellValueFactory(new PropertyValueFactory<>("cause"));

        TableColumn<ExceptionDescriptor, LocalDateTime> causeMessageColumn = new TableColumn<>();
        FXResourceBundle.getBundle().put(causeMessageColumn.textProperty(), "causeMessage");
        causeMessageColumn.setCellValueFactory(new PropertyValueFactory<>("causeMessage"));

        exceptionTable.getColumns().addAll(nameColumn, messageColumn, causeColumn, causeMessageColumn, timestampColumn);

        exceptionTable.setItems(Sys.em().getExceptionDescriptors());

        Button remove = new Button();
        remove.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        FXResourceBundle.getBundle().put(remove.textProperty(), "remove");
        remove.disableProperty().bind(Bindings.isEmpty(exceptionTable.getSelectionModel().getSelectedItems()));

        remove.setOnAction(e -> {
            ObservableList<ExceptionDescriptor> selectedItems = FXCollections.observableArrayList(exceptionTable.getSelectionModel().getSelectedItems());
            Sys.em().getExceptionDescriptors().removeAll(selectedItems);
        });

        TilePane buttonPane = LayoutUtils.createTilePane(List.of(remove));

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(exceptionTable);
        borderPane.setBottom(buttonPane);

        tab.setContent(borderPane);

        return tab;
    }

    private Tab createStackTraceTab() {
        Tab tab = new Tab();
        FXResourceBundle.getBundle().put(tab.textProperty(), "stackTrace");
        tab.setClosable(false);

        TextArea textArea = new TextArea();
        textArea.setEditable(false);

        exceptionTable.getSelectionModel().getSelectedItems().addListener((Change<? extends ExceptionDescriptor> c) -> {

            if (c.getList().size() == 1) {
                textArea.setText(c.getList().get(0).getTraceStack());
            } else {
                textArea.setText("");
            }
        });

        tab.setContent(textArea);


        return tab;
    }
}

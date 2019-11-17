package dev.jfxde.sysapps.editor;

import java.nio.file.Path;

import dev.jfxde.jfx.scene.control.AutoCompleteField;
import dev.jfxde.jfx.scene.control.InternalDialog;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXPath;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

public class FindFileDialog extends InternalDialog {

    private ObservableList<FXPath> paths = FXCollections.synchronizedObservableList(FXCollections.observableArrayList());

    public FindFileDialog(Node node) {
        super(node, Modality.WINDOW_MODAL);
        setTitle(FXResourceBundle.getBundle().getString​("findFiles"));

        AutoCompleteField<String> findField = new AutoCompleteField<String>();

        TableColumn<FXPath, ReadOnlyObjectProperty<Path>> pathColumn = new TableColumn<>();
        pathColumn.setText(FXResourceBundle.getBundle().getString​("path"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));

        TableView<FXPath> tableView = new TableView<FXPath>(paths);
        tableView.setPrefHeight(200);
        tableView.getColumns().addAll(pathColumn);


        BorderPane borderPane = new BorderPane();
        borderPane.setTop(findField);
        borderPane.setCenter(tableView);

        setContent(borderPane);
    }
}

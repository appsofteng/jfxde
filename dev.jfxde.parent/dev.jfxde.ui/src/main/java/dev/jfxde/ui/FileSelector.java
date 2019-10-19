package dev.jfxde.ui;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

public class FileSelector extends InternalDialog {

    private BorderPane borderPane = new BorderPane();
    private TreeView<PathDescriptor> fileTree;
    private TableView<TreeItem<PathDescriptor>> fileTable = new TableView<>();
    private Label fileLabel = new Label("File name:");
    private TextField fileField = new TextField();
    private ChoiceBox<String> filter = new ChoiceBox<>();
    private ButtonBar buttonBar = new ButtonBar();
    private Button okButton = new Button("OK");
    private Button cancelButton = new Button("Cancel");

    public FileSelector(Node node) {
        super(node, true);

        setGraphics();
        setBehavior();
    }

    private void setGraphics() {
        double height = windowPane.getHeight() * 0.8;
        var root = new LazyTreeItem<>(new PathDescriptor(Paths.get(""), "root"))
                .leaf(i -> i.getValue().isLeaf())
                .childrenGetter(i -> i.getValue().getDirectories(p -> new LazyTreeItem<>(p, i)))
                .graphic(i -> {
                    var icon = getIcon(i.getValue().getPath());

                    return icon;
                });

        fileTree = new TreeView<>(root);
        fileTree.setShowRoot(false);
        fileTree.setPrefHeight(height);

        TableColumn<TreeItem<PathDescriptor>, StringProperty> nameColumn = new TableColumn<>();
        nameColumn.setText("Name");
        nameColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValue().nameProperty()));
        nameColumn.setComparator(new PathDescriptor.StringComparator());
        nameColumn.setCellFactory(c -> {

            var cell = new TableCell<TreeItem<PathDescriptor>, StringProperty>() {
                @Override
                protected void updateItem(StringProperty item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.get());

                        var treeItem = getTableRow().getItem();
                        if (treeItem != null) {
                            setGraphic(getIcon(treeItem.getValue().getPath()));
                        }
                    }
                }
            };

            return cell;
        });

        TableColumn<TreeItem<PathDescriptor>, String> createdColumn = new TableColumn<>();
        createdColumn.setText("Created");

        TableColumn<TreeItem<PathDescriptor>, String> modifiedColumn = new TableColumn<>();
        modifiedColumn.setText("Modified");

        TableColumn<TreeItem<PathDescriptor>, String> sizeColumn = new TableColumn<>();
        sizeColumn.setText("Size");

        fileTable.getColumns().addAll(nameColumn, createdColumn, modifiedColumn, sizeColumn);
        fileTable.setPrefHeight(height);

        SplitPane splitPane = new SplitPane(fileTree, fileTable);
        splitPane.setDividerPositions(0.2f);
        borderPane.setCenter(splitPane);

        buttonBar.getButtons().addAll(okButton, cancelButton);
        buttonBar.setPadding(new Insets(5, 0, 0, 0));

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5, 10, 10, 10));
        GridPane.setMargin(fileField, new Insets(0, 5, 0, 5));
        ColumnConstraints column1 = new ColumnConstraints();
        column1.setHgrow(Priority.ALWAYS);
        gridpane.getColumnConstraints().addAll(new ColumnConstraints(), column1);

        gridpane.add(fileLabel, 0, 0, 1, 1);
        gridpane.add(fileField, 1, 0, 1, 1);
        gridpane.add(filter, 2, 0, 1, 1);
        gridpane.add(buttonBar, 0, 1, 3, 1);

        borderPane.setBottom(gridpane);
    }

    private void setBehavior() {

        fileTree.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                fileTable.getItems().setAll(n.getChildren());
                List<LazyTreeItem<PathDescriptor>> files = n.getValue().getFiles(p -> new LazyTreeItem<>(p,(LazyTreeItem<PathDescriptor>)n));
                fileTable.getItems().addAll(files);
            }
        });

        fileTable.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
                var treeItem = fileTable.getSelectionModel().getSelectedItem();

                if (treeItem != null && Files.isDirectory(treeItem.getValue().getPath())) {
                    fileTable.getItems().setAll(treeItem.getChildren());
                    fileTable.getItems().addAll(treeItem.getValue().getFiles(p -> new LazyTreeItem<>(p,(LazyTreeItem<PathDescriptor>)treeItem)));
                }
            }
        });

        okButton.setOnAction(e -> {
            close();
        });

        cancelButton.setOnAction(e -> close());
    }

    private ImageView getIcon(Path path) {
        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
        BufferedImage image = (BufferedImage) icon.getImage();
        Image fxIcon = SwingFXUtils.toFXImage(image, null);
        ImageView imageView = (new ImageView(fxIcon));

        return imageView;
    }

    public void show() {
        show(borderPane);
    }
}

package dev.jfxde.ui;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import org.controlsfx.control.BreadCrumbBar;

import dev.jfxde.jfxext.control.LazyTreeItem;
import dev.jfxde.logic.data.PathDescriptor;
import javafx.animation.PauseTransition;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Modality;
import javafx.util.Duration;

public class FileDialog extends InternalDialog {

    private BorderPane borderPane = new BorderPane();
    private BreadCrumbBar<PathDescriptor> breadCrumbBar;
    private TreeView<PathDescriptor> fileTree;
    private TableView<LazyTreeItem<PathDescriptor>> fileTable = new TableView<>();
    private ListView<LazyTreeItem<PathDescriptor>> selectionView = new ListView<>();
    private ButtonBar buttonBar = new ButtonBar();
    private Button okButton = new Button("OK");
    private Button cancelButton = new Button("Cancel");

    private Set<LazyTreeItem<PathDescriptor>> selection = new HashSet<>();
    private Consumer<List<Path>> selectionConsumer;

    private LazyTreeItem<PathDescriptor> root;
    private Consumer<LazyTreeItem<PathDescriptor>> filesGetter = i -> i.getValue().getFiles(p -> new LazyTreeItem<>(p, i), c -> i.addFilteredCached(c));
    private boolean allPaths = true;

    public FileDialog(Node node) {
        super(node, Modality.WINDOW_MODAL);

        setGraphics();
        setSelectionContextMenu();
        setBehavior();
    }

    @Override
    public FileDialog setTitle(String value) {
        super.setTitle(value);
        return this;
    }

    private void setGraphics() {
        double height = windowPane.getHeight() * 0.8;
        root = new LazyTreeItem<>(new PathDescriptor())
                .leaf(i -> i.getValue().isLeaf())
                .childrenGetter(i -> i.getValue().getDirectories(p -> new LazyTreeItem<>(p, i), c -> i.addCached(c)))
                .filteredChildrenGetter(filesGetter)
                .toString(i -> i.getValue().getPath().toString())
                .graphic(i -> getIcon(i.getValue().getPath()));

        breadCrumbBar = new BreadCrumbBar<>(root);

        fileTree = new TreeView<>(root);
        fileTree.setShowRoot(false);
        fileTree.setPrefHeight(height);

        TableColumn<LazyTreeItem<PathDescriptor>, StringProperty> nameColumn = new TableColumn<>();
        nameColumn.setText("Name");
        nameColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValue().nameProperty()));
        nameColumn.setComparator(new PathDescriptor.StringComparator());
        nameColumn.setPrefWidth(200);
        nameColumn.setCellFactory(c -> {

            var cell = new TableCell<LazyTreeItem<PathDescriptor>, StringProperty>() {
                @Override
                protected void updateItem(StringProperty item, boolean empty) {
                    super.updateItem(item, empty);

                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item.get());
                        var row = getTableRow();
                        var treeItem = row == null ? null : row.getItem();

                        if (treeItem != null) {
                            setGraphic(new ImageView(((ImageView) treeItem.getGraphic()).getImage()));
                        }
                    }
                }
            };

            return cell;
        });

        TableColumn<LazyTreeItem<PathDescriptor>, ReadOnlyObjectProperty<FileTime>> createdColumn = new TableColumn<>();
        createdColumn.setText("Created");
        createdColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValue().createdProperty()));
        createdColumn.setComparator(new PathDescriptor.ObjectComparator<>());
        createdColumn.setPrefWidth(120);

        TableColumn<LazyTreeItem<PathDescriptor>, ReadOnlyObjectProperty<FileTime>> modifiedColumn = new TableColumn<>();
        modifiedColumn.setText("Modified");
        modifiedColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValue().modifiedProperty()));
        modifiedColumn.setComparator(new PathDescriptor.ObjectComparator<>());
        modifiedColumn.setPrefWidth(120);

        TableColumn<LazyTreeItem<PathDescriptor>, ReadOnlyLongProperty> sizeColumn = new TableColumn<>();
        sizeColumn.setText("Size");
        sizeColumn.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue().getValue().sizeProperty()));
        sizeColumn.setComparator(new PathDescriptor.LongComparator());
        sizeColumn.getStyleClass().add("jd-table-column-numerical");
        sizeColumn.setPrefWidth(100);

        fileTable.getColumns().addAll(nameColumn, createdColumn, modifiedColumn, sizeColumn);
        fileTable.setPrefHeight(height);
        fileTable.setItems(root.getAllChildren());
        fileTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        SplitPane splitPane = new SplitPane(fileTree, fileTable);
        splitPane.setDividerPositions(0.2f);

        buttonBar.getButtons().addAll(okButton, cancelButton);

        GridPane gridpane = new GridPane();
        gridpane.setPadding(new Insets(5, 10, 10, 10));
        GridPane.setMargin(selectionView, new Insets(5, 0, 5, 0));
        ColumnConstraints column0 = new ColumnConstraints();
        column0.setHgrow(Priority.ALWAYS);
        gridpane.getColumnConstraints().addAll(column0);

        selectionView.setPrefHeight(100);
        selectionView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        gridpane.add(selectionView, 0, 0, 1, 1);
        gridpane.add(buttonBar, 0, 1, 1, 1);

        borderPane.setTop(breadCrumbBar);
        borderPane.setCenter(splitPane);
        borderPane.setBottom(gridpane);
    }

    private void setSelectionContextMenu() {

        MenuItem removeSelection = new MenuItem("Remove Selection");
        removeSelection.disableProperty().bind(selectionView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            selection.removeAll(selectionView.getSelectionModel().getSelectedItems());
            selectionView.getItems().removeAll(selectionView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(removeSelection);
        selectionView.setContextMenu(menu);
    }

    private void setBehavior() {

        breadCrumbBar.setOnCrumbAction(e -> {
            var treeItem = (LazyTreeItem<PathDescriptor>) e.getSelectedCrumb();
            selection.addAll(selectionView.getItems());
            fileTable.setItems(treeItem.getAllChildren());
        });

        fileTree.setOnMousePressed(e -> {
            if (e.isPrimaryButtonDown() && e.getClickCount() == 1) {
                var treeItem = (LazyTreeItem<PathDescriptor>) fileTree.getSelectionModel().getSelectedItem();

                if (treeItem != null) {
                    selection.addAll(selectionView.getItems());
                    fileTable.setItems(treeItem.getAllChildren());
                    breadCrumbBar.setSelectedCrumb(treeItem);
                }
            }
        });

        PauseTransition singlePressPause = new PauseTransition(Duration.millis(500));
        singlePressPause.setOnFinished(e -> {
            selectionView.getItems().setAll(fileTable.getSelectionModel().getSelectedItems().stream().filter(i -> i.getValue().isFile() || allPaths).collect(Collectors.toList()));
            selectionView.getItems().addAll(selection.stream().filter(i -> !selectionView.getItems().contains(i)).collect(Collectors.toList()));
            FXCollections.sort(selectionView.getItems(), Comparator.comparing(i -> i.getValue().getPath()));
        });

        fileTable.setOnMousePressed(e -> {

            if (e.isPrimaryButtonDown() && e.getClickCount() == 1) {
                singlePressPause.play();
            }

            if (e.isPrimaryButtonDown() && e.getClickCount() == 2) {
                singlePressPause.stop();
                var treeItem = fileTable.getSelectionModel().getSelectedItem();

                if (treeItem != null && treeItem.getValue().isDirectory() && treeItem.getValue().isReadable()) {
                    selection.addAll(selectionView.getItems());
                    fileTable.setItems(treeItem.getAllChildren());
                    breadCrumbBar.setSelectedCrumb(treeItem);
                }
            }
        });

        okButton.setOnAction(e -> {
            close();
            selection.addAll(selectionView.getItems());
            selectionConsumer.accept(selection.stream().map(i -> i.getValue().getPath()).collect(Collectors.toList()));
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

    public FileDialog directoriesOnly() {
        root.filteredChildrenGetter(i -> {});
        allPaths = true;
        return this;
    }

    public FileDialog filesOnly() {
        root.filteredChildrenGetter(filesGetter);
        allPaths = false;

        return this;
    }

    public void showOpenDialog(Consumer<List<Path>> consumer) {
        this.selectionConsumer = consumer;
        setContent(borderPane);
        show();
    }
}

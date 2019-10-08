package dev.jfxde.sysapps.jshell;



import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

public class SetBox extends VBox {

    private AppContext context;
    private Settings settings;
    private CheckBox defaultCheck;
    private CheckBox printingCheck;
    private ListView<String> filesView;

    public SetBox(AppContext context, Settings settings) {
        this.context = context;
        this.settings = settings;
        setGraphics();
        setBehavior();
        setContextMenu();
    }

    private void setGraphics() {
        defaultCheck = new CheckBox(context.rc().getString("default"));
        defaultCheck.setTooltip(new Tooltip());
        defaultCheck.getTooltip().setText(context.rc().getString("/open.default"));
        defaultCheck.setSelected(settings.isLoadDefault());

        printingCheck = new CheckBox(context.rc().getString("printing"));
        printingCheck.setTooltip(new Tooltip());
        printingCheck.getTooltip().setText(context.rc().getString("/open.printing"));
        printingCheck.setSelected(settings.isLoadPrinting());

        Label filesLabel = new Label(context.rc().getString("loadFiles"));
        filesView = new ListView<>(FXCollections.observableList(settings.getLoadFiles()));
        filesView.setPrefSize(500, 300);

        getChildren().addAll(defaultCheck, printingCheck, filesLabel, filesView);
    }

    private void setBehavior() {
        defaultCheck.setOnAction(e -> settings.setLoadDefault(defaultCheck.isSelected()));
        printingCheck.setOnAction(e -> settings.setLoadPrinting(printingCheck.isSelected()));
    }

    private void setContextMenu() {
        MenuItem addFiles = new MenuItem(context.rc().getString("addFiles"));
        addFiles.setOnAction(e -> {
            filesView.getItems().addAll(getFiles(settings.getLoadFiles()));
        });


        MenuItem removeSelection = new MenuItem(context.rc().getString("removeSelection"));
        removeSelection.disableProperty().bind(filesView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            filesView.getItems().removeAll(filesView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(addFiles, removeSelection);
        filesView.setContextMenu(menu);
    }

    private List<String> getFiles(List<String> current) {
        FileChooser chooser = new FileChooser();
        List<File> files = chooser.showOpenMultipleDialog(getScene().getWindow());

        List<String> paths = files == null ? List.of()
                : files.stream().map(f -> f.toString()).filter(p -> !current.contains(p)).collect(Collectors.toList());

        return paths;
    }

    public Settings getSettings() {
        return settings;
    }
}

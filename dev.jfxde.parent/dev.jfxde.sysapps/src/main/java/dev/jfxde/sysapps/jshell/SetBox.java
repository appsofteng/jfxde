package dev.jfxde.sysapps.jshell;



import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.api.AppContext;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
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
    private CheckBox scriptsCheck;
    private ListView<String> scriptsView;

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


        scriptsCheck = new CheckBox(context.rc().getString("loadScripts"));
        scriptsCheck.setTooltip(new Tooltip());
        scriptsCheck.getTooltip().setText(context.rc().getString("/open.loadScripts"));
        scriptsCheck.setSelected(settings.isLoadScripts());
        scriptsCheck.setPadding(new Insets(5,0,0,0));

        scriptsView = new ListView<>(FXCollections.observableList(settings.getStartupScripts()));
        scriptsView.setPrefSize(500, 300);
        scriptsView.setMinHeight(300);
        scriptsView.setMaxHeight(300);

        getChildren().addAll(defaultCheck, printingCheck, scriptsCheck, scriptsView);
    }

    private void setBehavior() {
        defaultCheck.setOnAction(e -> settings.setLoadDefault(defaultCheck.isSelected()));
        printingCheck.setOnAction(e -> settings.setLoadPrinting(printingCheck.isSelected()));
        scriptsCheck.setOnAction(e -> settings.setLoadScripts(scriptsCheck.isSelected()));
    }

    private void setContextMenu() {
        MenuItem addFiles = new MenuItem(context.rc().getString("addScripts"));
        addFiles.setOnAction(e -> {
            scriptsView.getItems().addAll(getFiles(settings.getStartupScripts()));
        });


        MenuItem removeSelection = new MenuItem(context.rc().getString("removeSelection"));
        removeSelection.disableProperty().bind(scriptsView.getSelectionModel().selectedIndexProperty().isEqualTo(-1));
        removeSelection.setOnAction(e -> {
            scriptsView.getItems().removeAll(scriptsView.getSelectionModel().getSelectedItems());
        });

        ContextMenu menu = new ContextMenu(addFiles, removeSelection);
        scriptsView.setContextMenu(menu);
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

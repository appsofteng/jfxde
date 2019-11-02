package dev.jfxde.sysapps.editor;

import dev.jfxde.logic.data.FXPath;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class EditorPane extends StackPane {

    private TabPane tabPane = new TabPane();

    public EditorPane() {
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);

        getChildren().add(tabPane);
    }

    void setEditor(FXPath path) {
        Tab tab = findEditorTab(path);

        if (tab == null) {
            tab = createEditorTab(path);
            tabPane.getTabs().add(tab);
        }

        tabPane.getSelectionModel().select(tab);
    }

    private Tab findEditorTab(FXPath path) {
        return tabPane.getTabs().stream()
            .filter(t -> ((Editor)t.getContent()).getPath().equals(path))
            .findFirst()
            .orElse(null);
    }

    private Tab createEditorTab(FXPath path) {
        Tab tab = new Tab();
        Editor editor = new Editor(path);
        tab.setContent(editor);

        tab.closableProperty().bind(editor.editedProperty().not());
        Label label = new Label();
        label.textProperty().bind(editor.tabTitleProperty());
        tab.setGraphic(label);
        addContextMenu(tab);

        Tooltip tooltip = new Tooltip();
        tooltip.textProperty().bind(editor.titleProperty());
        tooltip.setShowDuration(Duration.seconds(3600));
        tab.setTooltip(tooltip);

        return tab;
    }

    private void addContextMenu(Tab tab) {

    }
}

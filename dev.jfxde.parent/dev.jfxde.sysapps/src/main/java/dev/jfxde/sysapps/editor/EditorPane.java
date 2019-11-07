package dev.jfxde.sysapps.editor;

import dev.jfxde.logic.data.FXPath;
import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
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
    private final ObservableList<Editor> editors = FXCollections.observableArrayList(e -> new Observable[] { e.editedProperty() });
    private final FilteredList<Editor> closableEditors = editors.filtered(e -> !e.isEdited());
    private final ObjectProperty<Editor> selectedEditor = new SimpleObjectProperty<>();

    public EditorPane() {
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);

        setListeners();
        getChildren().add(tabPane);
    }

    private void setListeners() {
        tabPane.getSelectionModel().selectedItemProperty().addListener((v,o,n) -> {
            if (n != null) {
                Editor editor = (Editor)n.getContent();
                selectedEditor.set(editor);
            } else {
                selectedEditor.set(null);
            }
        });
    }

    ReadOnlyObjectProperty<Editor> selectedEditorProperty() {
        return selectedEditor;
    }

    Editor getSelectedEditor() {
        return selectedEditor.get();
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

    void save() {
        tabPane.getTabs().stream().filter(Tab::isSelected).forEach(t -> ((Editor)t.getContent()).save());
    }
}

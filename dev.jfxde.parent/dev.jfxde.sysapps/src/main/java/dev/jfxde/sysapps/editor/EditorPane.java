package dev.jfxde.sysapps.editor;

import java.util.List;

import dev.jfxde.jfx.application.XPlatform;
import dev.jfxde.jfx.util.FXResourceBundle;
import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePosition;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyBooleanWrapper;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class EditorPane extends StackPane {

    private EditorActions actions;
    private TabPane tabPane = new TabPane();
    private final ObservableList<Editor> editors = FXCollections
            .observableArrayList(e -> new Observable[] { e.changedProperty(), e.deletedProperty() });
    private final FilteredList<Editor> closableEditors = editors.filtered(e -> !e.isChanged());
    private final ObjectProperty<Editor> selectedEditor = new SimpleObjectProperty<>();
    private final ReadOnlyBooleanWrapper changed = new ReadOnlyBooleanWrapper();
    private FindDialog findDialog;

    public EditorPane(EditorActions actions) {
        this.actions = actions;
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);

        setListeners();
        getChildren().add(tabPane);
    }

    private void setListeners() {
        changed.bind(Bindings.createBooleanBinding(() -> editors.stream().anyMatch(Editor::isChanged), editors));

        tabPane.getTabs().addListener((Change<? extends Tab> c) -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(t -> {
                        Editor editor = (Editor) t.getContent();
                        editors.add(editor);
                    });
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(t -> {
                        Editor editor = (Editor) t.getContent();
                        editors.remove(editor);
                        editor.dispose();
                    });
                }
            }
        });

        editors.addListener((Change<? extends Editor> c) -> {
            while (c.next()) {
                if (c.wasUpdated()) {
                    XPlatform.runFX(() -> {
                        tabPane.getTabs().removeIf(t -> ((Editor) t.getContent()).isDeleted());
                    });
                }
            }
        });

        tabPane.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {

            if (findDialog != null) {
                findDialog.foundCountProperty().unbind();
            }

            if (n != null) {
                Editor editor = (Editor) n.getContent();
                selectedEditor.set(editor);
                if (findDialog != null) {
                    findDialog.foundCountProperty().bind(editor.getCodeAreaWrappers().getFindWrapper().foundCountProperty());
                }
            } else {
                selectedEditor.set(null);
            }
        });
    }

    ReadOnlyBooleanProperty changedProperty() {
        return changed;
    }

    ReadOnlyObjectProperty<Editor> selectedEditorProperty() {
        return selectedEditor;
    }

    Editor getSelectedEditor() {
        return selectedEditor.get();
    }

    void open(List<FilePosition> positions) {
        positions.forEach(p -> setEditor(p));
    }

    private void setEditor(FilePosition filePosition) {
        Tab tab = findEditorTab(filePosition.getPath());

        if (tab == null) {
            tab = createEditorTab(filePosition);
            tabPane.getTabs().add(tab);
        } else {
            ((Editor)tab.getContent()).moveToPotition(filePosition);
        }

        tabPane.getSelectionModel().select(tab);
    }

    private Tab findEditorTab(FXPath path) {
        return tabPane.getTabs().stream()
                .filter(t -> ((Editor) t.getContent()).getPath().equals(path))
                .findFirst()
                .orElse(null);
    }

    private Tab createEditorTab(FilePosition filePosition) {
        Tab tab = new Tab();
        Editor editor = new Editor(filePosition, actions);
        tab.setContent(editor);

        tab.closableProperty().bind(editor.changedProperty().not());
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

        MenuItem reload = new MenuItem();
        FXResourceBundle.getBundle().put(reload.textProperty(), "reload");
        reload.disableProperty().bind(((Editor)tab.getContent()).deletedExternallyProperty());
        reload.setOnAction(e -> ((Editor)tab.getContent()).load());

        MenuItem closeOthers = new MenuItem();
        FXResourceBundle.getBundle().put(closeOthers.textProperty(), "closeOthers");
        closeOthers.disableProperty().bind(Bindings.isEmpty(closableEditors).or(Bindings.size(closableEditors).isEqualTo(1).and(tab.closableProperty())));
        closeOthers.setOnAction(e -> tabPane.getTabs().removeIf(t -> t != tab && !((Editor)t.getContent()).isChanged()));

        MenuItem closeAll = new MenuItem();
        FXResourceBundle.getBundle().put(closeAll.textProperty(), "closeAll");
        closeAll.disableProperty().bind(Bindings.isEmpty(closableEditors));
        closeAll.setOnAction(e -> tabPane.getTabs().removeIf(t -> !((Editor)t.getContent()).isChanged()));

        MenuItem close = new MenuItem();
        FXResourceBundle.getBundle().put(close.textProperty(), "closeWithoutSaving");
        close.setOnAction(e -> tabPane.getTabs().remove(tab));

        ContextMenu menu = new ContextMenu(reload, new SeparatorMenuItem(), closeOthers, closeAll, new SeparatorMenuItem(), close);
        tab.setContextMenu(menu);
    }

    void save() {
        tabPane.getTabs().stream().filter(Tab::isSelected).forEach(t -> ((Editor) t.getContent()).save());
    }

    void saveAll() {
        tabPane.getTabs().forEach(t -> ((Editor) t.getContent()).save());
    }

    void find() {

        if (findDialog == null) {
            findDialog = new FindDialog(this)
                    .findPrevious(getSelectedEditor().getCodeAreaWrappers().getFindWrapper()::findPrevious)
                    .findNext(getSelectedEditor().getCodeAreaWrappers().getFindWrapper()::findNext)
                    .replace(getSelectedEditor().getCodeAreaWrappers().getFindWrapper()::replace)
                    .replaceAll(getSelectedEditor().getCodeAreaWrappers().getFindWrapper()::replaceAll);

            findDialog.parentProperty().addListener((v, o, n) -> {
                if (n == null) {
                    findDialog = null;
                }
            });

            findDialog.foundCountProperty().bind(getSelectedEditor().getCodeAreaWrappers().getFindWrapper().foundCountProperty());
        }

        findDialog.text(getSelectedEditor().getArea().getSelectedText())
                .show();
    }
}

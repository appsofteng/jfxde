package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.Desktop;
import dev.jfxde.logic.data.Window;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.layout.Pane;

public class WindowPane extends Pane {

	private Desktop desktop;
    private ObservableList<AppWindow> windows;
    private FilteredList<AppWindow> visibleWindows;
    private FilteredList<AppWindow> closableWindows;

    private IntegerProperty tileCols = new SimpleIntegerProperty();
    private IntegerProperty tileRows = new SimpleIntegerProperty();
    private DoubleProperty tileWidth = new SimpleDoubleProperty();
    private DoubleProperty tileHeight = new SimpleDoubleProperty();

	public WindowPane(Desktop desktop) {
		this.desktop = desktop;
        setPickOnBounds(false);
        getStyleClass().add("jd-desktop-window-pane");
        windows = FXCollections.observableArrayList(w -> new Observable[] { w.visibleProperty() });

        visibleWindows = windows.filtered(w -> w.isVisible());
        closableWindows = visibleWindows;

        tileCols.bind(Bindings.when(Bindings.size(visibleWindows).greaterThan(1)).then(2).otherwise(1));
        tileRows.bind(Bindings.createIntegerBinding(() -> visibleWindows.size() / tileCols.get() + (int) Math.signum(visibleWindows.size() % tileCols.get()), Bindings.size(visibleWindows), tileCols));
        tileWidth.bind(widthProperty().divide(tileCols));
        tileHeight.bind(heightProperty().divide(tileRows));

        initListeners();
	}

    public ObservableList<AppWindow> getVisibleWindows() {
        return visibleWindows;
    }

    public ObservableList<AppWindow> getClosableWindows() {
        return closableWindows;
    }

    public IntegerProperty tileColsProperty() {
        return tileCols;
    }

    public IntegerProperty tileRowsProperty() {
        return tileRows;
    }

    public DoubleProperty tileWidthProperty() {
        return tileWidth;
    }

    public DoubleProperty tileHeightProperty() {
        return tileHeight;
    }

	private void initListeners() {

        desktop.getWindows().addListener((Change<? extends Window> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    c.getAddedSubList().forEach(a -> addWindow(a));
                } else if (c.wasRemoved()) {
                    c.getRemoved().forEach(a -> removeWindow(a));
                }
            }
        });
	}

    void addWindow(Window window) {

        AppWindow appWindow = new AppWindow(window, this);

        getChildren().add(appWindow);
        windows.add(appWindow);
    }

    void removeWindow(Window window) {
        AppWindow appWindow = (AppWindow) getChildren().stream().filter(ch -> ((AppWindow) ch).getWindow() == window).findFirst().orElseGet(null);

        if (appWindow != null) {
            getChildren().remove(appWindow);
            windows.remove(appWindow);
            appWindow.dispose();
        }
    }
}

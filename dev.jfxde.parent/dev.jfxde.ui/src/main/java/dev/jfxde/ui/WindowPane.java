package dev.jfxde.ui;

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
    private ObservableList<InternalWindow> windows;
    private FilteredList<InternalWindow> visibleWindows;
    private ObservableList<InternalWindow> tiledWindows = FXCollections.observableArrayList();
    private FilteredList<InternalWindow> closableWindows;

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

        tileCols.bind(Bindings.when(Bindings.size(tiledWindows).greaterThan(1)).then(2).otherwise(1));
        tileRows.bind(Bindings.createIntegerBinding(() -> tiledWindows.size() / tileCols.get() + (int) Math.signum(tiledWindows.size() % tileCols.get()), Bindings.size(tiledWindows), tileCols));
        tileWidth.bind(widthProperty().divide(tileCols));
        tileHeight.bind(heightProperty().divide(tileRows));

        initListeners();
	}

    public ObservableList<? extends InternalWindow> getVisibleWindows() {
        return visibleWindows;
    }

    public ObservableList<? extends InternalWindow> getTiledWindows() {
        return tiledWindows;
    }

    public void tile() {
        visibleWindows.forEach(w -> ((InternalWindow) w).getWindow().tile());
    }

    public void tile(InternalWindow window) {
        tiledWindows.add(window);
    }

    public void untile(InternalWindow window) {
        tiledWindows.remove(window);
    }

    public ObservableList<? extends InternalWindow> getClosableWindows() {
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

        InternalWindow appWindow = new AppWindow(this, window);

        getChildren().add(appWindow);
        windows.add(appWindow);
    }

    void removeWindow(Window window) {
        InternalWindow appWindow = (InternalWindow) getChildren().stream().filter(c -> c instanceof InternalWindow).filter(c -> ((InternalWindow) c).getWindow() == window).findFirst().orElseGet(null);

        if (appWindow != null) {
            getChildren().remove(appWindow);
            windows.remove(appWindow);
            tiledWindows.remove(appWindow);
            appWindow.dispose();
        }
    }
}

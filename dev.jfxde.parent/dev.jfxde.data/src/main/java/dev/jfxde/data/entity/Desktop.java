package dev.jfxde.data.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Desktop extends DataObj {

    private int _id;
    private boolean _active;
    private List<Shortcut> _shortcuts;

    private transient IntegerProperty id;
    private transient BooleanProperty active;

    private transient ObservableList<Window> windows;
    private transient ObjectProperty<Window> activeWindow;
    private transient ObjectProperty<Window> previousActiveWindow;

    private transient ObservableList<Shortcut> shortcuts;
    private transient ObjectProperty<Shortcut> activeShortcut;

    public Desktop() {

    }

    public Desktop(int id) {
        this.setId(id);
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean value) {
        activeProperty().set(value);
    }

    public BooleanProperty activeProperty() {

        if (active == null) {
            active = new SimpleBooleanProperty(_active) {
                @Override
                public void set(boolean newValue) {
                    _active = newValue;
                    super.set(newValue);
                }
            };
        }

        return active;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        idProperty().set(id);
    }

    public IntegerProperty idProperty() {
        if (id == null) {
            id = new SimpleIntegerProperty(_id) {
                @Override
                public void set(int newValue) {
                    _id = newValue;
                    super.set(newValue);
                }
            };
        }

        return id;
    }

    public ObservableList<Window> getWindows() {
        if (windows == null) {
            windows = FXCollections.observableArrayList();
        }

        return windows;
    }

    public ReadOnlyObjectProperty<Window> activeWindowProperty() {

        return getActiveWindowProperty();
    }

    private ObjectProperty<Window> getActiveWindowProperty() {

        if (activeWindow == null) {
            activeWindow = new SimpleObjectProperty<>();
        }

        return activeWindow;
    }

    private ObjectProperty<Window> previousActiveWindowProperty() {

        if (previousActiveWindow == null) {
            previousActiveWindow = new SimpleObjectProperty<>();
        }

        return previousActiveWindow;
    }

    public Window getActiveWindow() {
        return activeWindowProperty().get();
    }

    public void setActiveWindow(Window window) {

        previousActiveWindowProperty().set(getActiveWindow());

        if (getActiveWindow() == window) {
            return;
        }

        if (getActiveWindow() != null) {
            getActiveWindow().setActive(false);
        }

        if (window != null) {
            setActiveShortcut(null);
            window.setActive(true);
        }

        getActiveWindowProperty().set(window);
    }

    public void addWindow(Window window) {
        if (getWindows().contains(window)) {
            setActiveWindow(window);
            return;
        }

        if (window.getDesktop() == null) {
            window.setDesktop(this);
        } else if (window.getDesktop() != this) {
            window.getDesktop().removeWindow(window);
            window.setDesktop(this);
        }

        window.reset();
        getWindows().add(window);

        setActiveWindow(window);
    }

    public void removeWindow(Window window) {
        getWindows().remove(window);
        window.setDesktop(null);

        if (getActiveWindow() == window) {
            activateWindow();
        }

        if (previousActiveWindowProperty().get() == window) {
            previousActiveWindowProperty().set(activeWindowProperty().get());
        }
    }

    public void activateWindow() {
        Window newActiveApp = getWindows().stream().filter(w -> !w.isMinimized()).reduce((first, second) -> second)
                .orElse(null);

        setActiveWindow(newActiveApp);
    }

    public void toPreviousActiveWindow() {
        setActiveWindow(previousActiveWindowProperty().get());

    }

    public void minimizeOthers() {
        getWindows().stream().filter(w -> w != getActiveWindow()).filter(a -> !a.isMinimized()).forEach(Window::minimize);
    }

    public void minimizeAll() {
        getWindows().stream().filter(w -> !w.isMinimized()).forEach(Window::minimize);
        setActiveWindow(null);
    }

    public void closeOthers() {
        List<Window> toBeRemoved = getWindows().stream().filter(w -> w != getActiveWindow()).collect(Collectors.toList());
        toBeRemoved.forEach(Window::close);
    }

    public void closeAll() {
        List<Window> toBeRemoved = new ArrayList<>(getWindows());
        toBeRemoved.forEach(Window::close);
    }

    public ObservableList<Shortcut> getShortcuts() {

        if (shortcuts == null) {
            shortcuts = FXCollections.observableList(_getShortcuts());
        }

        return shortcuts;
    }

    public List<Shortcut> _getShortcuts() {

        if (_shortcuts == null) {
            _shortcuts = new ArrayList<>();
        }

        return _shortcuts;
    }

    private ObjectProperty<Shortcut> activeShortcutProperty() {
        if (activeShortcut == null) {
            activeShortcut = new SimpleObjectProperty<>();
        }

        return activeShortcut;
    }

    public Shortcut getActiveShortcut() {
        return activeShortcut == null ? null : activeShortcut.get();
    }

    public void setActiveShortcut(Shortcut shortcut) {

        if (getActiveShortcut() == shortcut) {
            return;
        }

        if (getActiveShortcut() != null) {
            getActiveShortcut().setActive(false);
        }

        if (shortcut != null) {
            setActiveWindow(null);
            shortcut.setActive(true);
        }

        activeShortcutProperty().set(shortcut);
    }

    public void moveShortcut(Shortcut shortcut) {

        Shortcut sc = findWithSamePosition(shortcut);

        while (sc != null) {
            sc.setPosition(sc.getPosition() + 1);

            sc = findWithSamePosition(sc);
        }
    }

    private Shortcut findWithSamePosition(Shortcut shortcut) {
        Shortcut sc = getShortcuts().stream().filter(s -> s != shortcut)
                .filter(s -> s.getPosition() == shortcut.getPosition()).findFirst().orElse(null);

        return sc;
    }

    public void addShortcut(Shortcut shortcut) {
        if (getShortcuts().contains(shortcut)) {
            return;
        }

        if (shortcut.getDesktop() == null) {
            shortcut.setDesktop(this);
        } else if (shortcut.getDesktop() != this) {
            shortcut.getDesktop().removeShortcut(shortcut);
            shortcut.setDesktop(this);
        }

        shortcut.setPosition(getFirstFreePosition());
        getShortcuts().add(shortcut);
        setActiveShortcut(shortcut);
    }

    private int getFirstFreePosition() {

        int position = IntStream.rangeClosed(0, getShortcuts().size())
                .filter(i -> getShortcuts().stream().mapToInt(s -> s.getPosition()).noneMatch(p -> i == p)).findFirst()
                .orElse(0);

        return position;
    }

    public void removeShortcut(Shortcut shortcut) {
        getShortcuts().remove(shortcut);
        shortcut.setDesktop(null);

        if (getActiveShortcut() == shortcut) {
            activeShortcutProperty().set(null);
        }
    }

    @Override
    public Desktop copy() {
        var copy = new Desktop();
        copy._active = _active;
        copy._id = _id;
        copy._shortcuts = _shortcuts.stream().map(Shortcut::copy).collect(Collectors.toList());

        return copy;
    }
}

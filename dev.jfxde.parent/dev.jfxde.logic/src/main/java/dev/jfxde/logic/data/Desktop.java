package dev.jfxde.logic.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Desktop extends DataObj {

    private BooleanProperty active = new SimpleBooleanProperty();

    private ObservableList<Window> windows = FXCollections.observableArrayList();
    private ObjectProperty<Window> activeWindow = new SimpleObjectProperty<>();
    private ObjectProperty<Window> previousActiveWindow = new SimpleObjectProperty<>();

    private ObservableList<Shortcut> shortcuts = FXCollections.observableArrayList();
    private ObjectProperty<Shortcut> activeShortcut = new SimpleObjectProperty<>();

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean value) {
        active.set(value);
    }

    public ObservableList<Window> getWindows() {
        return windows;
    }

    public ReadOnlyObjectProperty<Window> activeWindowProperty() {
        return activeWindow;
    }

    public Window getActiveWindow() {
        return activeWindow.get();
    }

    public void setActiveWindow(Window window) {

        previousActiveWindow.set(activeWindow.get());

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

        activeWindow.set(window);
    }

    public void addWindow(Window window) {
        if (windows.contains(window)) {
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
        windows.add(window);

        setActiveWindow(window);
    }

    public void removeWindow(Window window) {
        windows.remove(window);
        window.setDesktop(null);

        if (getActiveWindow() == window) {
            activateWindow();
        }

        if (previousActiveWindow.get() == window) {
            previousActiveWindow.set(activeWindow.get());
        }
    }

    public void activateWindow() {
        Window newActiveApp = windows.stream().filter(w -> !w.isMinimized()).reduce((first, second) -> second)
                .orElse(null);

        setActiveWindow(newActiveApp);
    }

    public void toPreviousActiveWindow() {
        setActiveWindow(previousActiveWindow.get());

    }

    public void minimizeOthers() {
        windows.stream().filter(w -> w != getActiveWindow()).filter(a -> !a.isMinimized()).forEach(Window::minimize);
    }

    public void minimizeAll() {
        windows.stream().filter(w -> !w.isMinimized()).forEach(Window::minimize);
        setActiveWindow(null);
    }

    public void closeOthers() {
        List<Window> toBeRemoved = windows.stream().filter(w -> w != getActiveWindow()).collect(Collectors.toList());
        toBeRemoved.forEach(Window::close);
    }

    public void closeAll() {
        List<Window> toBeRemoved = new ArrayList<>(windows);
        toBeRemoved.forEach(Window::close);
    }

    public ObservableList<Shortcut> getShortcuts() {
        return shortcuts;
    }

    public Shortcut getActiveShortcut() {
        return activeShortcut.get();
    }

    public void setActiveShortcut(Shortcut shortcut) {

        if (getActiveShortcut() == shortcut) {
            return;
        }

        if (activeShortcut.get() != null) {
            activeShortcut.get().setActive(false);
        }

        if (shortcut != null) {
            setActiveWindow(null);
            shortcut.setActive(true);
        }

        activeShortcut.set(shortcut);
    }

    public void moveShortcut(Shortcut shortcut) {

        Shortcut sc = findWithSamePosition(shortcut);

        while (sc != null) {
            sc.setPosition(sc.getPosition() + 1);

            sc = findWithSamePosition(sc);
        }
    }

    private Shortcut findWithSamePosition(Shortcut shortcut) {
        Shortcut sc = shortcuts.stream().filter(s -> s != shortcut)
                .filter(s -> s.getPosition() == shortcut.getPosition()).findFirst().orElse(null);

        return sc;
    }

    public void addShortcut(Shortcut shortcut) {
        if (shortcuts.contains(shortcut)) {
            return;
        }

        if (shortcut.getDesktop() == null) {
            shortcut.setDesktop(this);
        } else if (shortcut.getDesktop() != this) {
            shortcut.getDesktop().removeShortcut(shortcut);
            shortcut.setDesktop(this);
        }

        shortcut.setPosition(getFirstFreePosition());
        shortcuts.add(shortcut);
        setActiveShortcut(shortcut);
    }

    private int getFirstFreePosition() {

        int position = IntStream.rangeClosed(0, shortcuts.size())
                .filter(i -> shortcuts.stream().mapToInt(s -> s.getPosition()).noneMatch(p -> i == p)).findFirst()
                .orElse(0);

        return position;
    }

    public void removeShortcut(Shortcut shortcut) {
        shortcuts.remove(shortcut);
        shortcut.setDesktop(null);

        if (getActiveShortcut() == shortcut) {
            activeShortcut.set(null);
        }
    }

}

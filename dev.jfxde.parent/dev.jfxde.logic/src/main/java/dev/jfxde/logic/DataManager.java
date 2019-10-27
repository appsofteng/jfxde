package dev.jfxde.logic;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.data.dao.StorageManager;
import dev.jfxde.data.entity.AppProviderData;
import dev.jfxde.data.entity.DataRoot;
import dev.jfxde.data.entity.Desktop;
import dev.jfxde.data.entity.Shortcut;
import dev.jfxde.data.entity.Window;
import dev.jfxde.logic.data.AppProviderDescriptor;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class DataManager extends Manager {

    private final ObjectProperty<Desktop> activeDesktop = new SimpleObjectProperty<>();
    private ObjectProperty<Window> activeWindow = new SimpleObjectProperty<>();
    private StorageManager storageManager;
    private DataRoot dataRoot = new DataRoot();

    DataManager() {
    }

    @Override
    void init() {
        storageManager = new StorageManager(dataRoot, FileManager.DB_DIR.toFile());

        initData();
    }

    private void initData() {

        List<Desktop> desktops = dataRoot.getDesktops();

        if (desktops.isEmpty()) {
            desktops = IntStream.range(1, 6).mapToObj(i -> new Desktop(i)).collect(Collectors.toList());
            desktops.get(0).setActive(true);
            dataRoot.getDesktops().addAll(desktops);
            storageManager.store(dataRoot.getDesktops());
        }

        Desktop foundActiveDesktop = desktops.stream().filter(Desktop::isActive).findFirst().get();

        activeDesktop.set(foundActiveDesktop);
        activeWindow.unbind();
        activeWindow.bind(foundActiveDesktop.activeWindowProperty());
    }

    public ObjectProperty<Desktop> activeDesktopProperty() {
        return activeDesktop;
    }

    public Desktop getActiveDesktop() {
        Desktop desktop = activeDesktop.get();

        return desktop;
    }

    public boolean setActiveDesktop(Desktop desktop) {

        if (desktop == getActiveDesktop()) {
            return false;
        }

        if (activeDesktop.get() != null) {
            var aDesktop = activeDesktop.get();
            aDesktop.setActive(false);
            storageManager.store(aDesktop);
        }

        desktop.setActive(true);
        storageManager.store(desktop);

        activeDesktop.set(desktop);
        activeWindow.unbind();
        activeWindow.bind(desktop.activeWindowProperty());

        return true;
    }

    public ReadOnlyObjectProperty<Window> activeWindowProperty() {
        return activeWindow;
    }

    public Window getActiveWindow() {
        return activeWindow.get();
    }

    public List<Desktop> getDesktops() {

        return dataRoot.getDesktops();
    }

    public void addShortcut(AppProviderDescriptor descriptor) {
        Shortcut shortcut = new Shortcut(descriptor.getAppProviderData());

        getActiveDesktop().addShortcut(shortcut);
        storageManager.store(getActiveDesktop()._getShortcuts());
    }

    public void removeShortcut(Shortcut shortcut) {

        var desktop = shortcut.getDesktop();
        shortcut.remove();
        storageManager.store(desktop._getShortcuts());
    }

    public void updateShortcut(Shortcut shortcut) {
        storageManager.store(shortcut);
    }

    public List<AppProviderData> getAppProviderData(String fqn) {

        List<AppProviderData> result = dataRoot.getAppProviders().stream().filter(p -> p.getFqn().equals(fqn))
                .collect(Collectors.toList());

        return result;
    }

    public void update(AppProviderData data) {

        var existing = dataRoot.getAppProviders().stream().filter(d -> d.getFqn().equals(data.getFqn())).findFirst().orElse(null);
        if (existing == null) {
            dataRoot.getAppProviders().add(data);
            storageManager.store(dataRoot.getAppProviders());
        } else {
            storageManager.store(data);
        }
    }

    void stop() {
        storageManager.shutdown();
    }
}

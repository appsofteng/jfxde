package dev.jfxde.logic;

import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import dev.jfxde.j.util.prefs.FilePreferencesFactory;
import dev.jfxde.logic.data.Preference;
import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class PreferencesManager extends Manager {

    public static final String USER_LOCALE = "/region/locale";
    public static final String USER_THEME_COLOR = "/theme/color";

    PreferencesManager() {
    }

    @Override
    void init() throws Exception {

        System.setProperty("dev.jfxde.j.util.prefs.defaultSystemRoot", FileManager.DEFAULT_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.j.util.prefs.systemRoot", FileManager.SYSTEM_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.j.util.prefs.defaultUserRoot", FileManager.DEFAULT_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.j.util.prefs.userRoot", FileManager.USER_PREFS_FILE.toString());
        System.setProperty("java.util.prefs.PreferencesFactory", FilePreferencesFactory.class.getName());

        ResourceManager.setLocale(getLocale());
    }

    public void setLocale(String locale) {

        ResourceManager.setLocale(locale);

        Sys.am().sortApp();
    }

    private String getLocale() {
        return Preferences.userRoot().node("/region").get("locale", "en");
    }

    public String getThemeColor() {
        return Preferences.userRoot().node("/theme").get("color", "black");
    }

    public ObservableList<PropertyDescriptor> getSystemProperties() {

        ObservableList<PropertyDescriptor> properties = FXCollections.observableArrayList(
                System.getProperties().entrySet().stream().map(PropertyDescriptor::new).collect(Collectors.toList()));
        FXCollections.sort(properties);

        return properties;
    }

    public <T> void getPreferences(Preference parent, Function<Preference, T> mapper, Consumer<T> consumer) {

        if (parent.getKey() == null) {
            consumer.accept(mapper.apply(new Preference("system", false)));
            consumer.accept(mapper.apply(new Preference("user", false)));
            consumer.accept(null);
        } else if ("system".equals(parent.getKey())) {
            getChildPreferences(Preferences.systemRoot(), mapper, consumer);
        } else if ("user".equals(parent.getKey())) {
            getChildPreferences(Preferences.userRoot(), mapper, consumer);
        } else {
            Preferences child = parent.getPreferences().node(parent.getKey());
            getChildPreferences(child, mapper, consumer);
        }
    }

    private <T> void getChildPreferences(Preferences child, Function<Preference, T> mapper, Consumer<T> consumer) {

        try {
            String[] chlidrenNames = child.childrenNames();

            Arrays.stream(chlidrenNames).map(n -> mapper.apply(new Preference(child, n, false)))
                    .forEach(consumer);
            String[] keys = child.keys();

            Arrays.stream(keys).map(n -> mapper.apply(new Preference(child, n, true)))
                    .forEach(consumer);
            consumer.accept(null);

        } catch (BackingStoreException e) {
            new RuntimeException(e);
        }
    }
}

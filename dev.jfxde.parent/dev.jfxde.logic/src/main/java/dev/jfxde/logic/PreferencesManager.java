package dev.jfxde.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.util.prefs.FilePreferencesFactory;
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

        System.setProperty("dev.jfxde.jfxext.util.prefs.defaultSystemRoot", FileManager.DEFAULT_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.jfxext.util.prefs.systemRoot", FileManager.SYSTEM_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.jfxext.util.prefs.defaultUserRoot", FileManager.DEFAULT_PREFS_FILE.toString());
        System.setProperty("dev.jfxde.jfxext.util.prefs.userRoot", FileManager.USER_PREFS_FILE.toString());
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

    public <T> List<T> getPreferences(Preference parent, Function<Preference, T> mapper) {
        List<T> preferences = new ArrayList<>();

        if (parent.getKey() == null) {
            preferences.add(mapper.apply(new Preference("system", false)));
            preferences.add(mapper.apply(new Preference("user", false)));
        } else if ("system".equals(parent.getKey())) {
            preferences = getChildPreferences(Preferences.systemRoot(), mapper);
        } else if ("user".equals(parent.getKey())) {
            preferences = getChildPreferences(Preferences.userRoot(), mapper);
        } else {
            Preferences child = parent.getPreferences().node(parent.getKey());
            preferences = getChildPreferences(child, mapper);
        }

        return preferences;
    }

    private <T> List<T> getChildPreferences(Preferences child, Function<Preference, T> mapper) {
        List<T> preferences = new ArrayList<>();

        try {
            String[] chlidrenNames = child.childrenNames();
            preferences.addAll(
                    Arrays.stream(chlidrenNames).map(n -> mapper.apply(new Preference(child, n, false)))
                            .collect(Collectors.toList()));
            String[] keys = child.keys();

            preferences.addAll(
                    Arrays.stream(keys).map(n -> mapper.apply(new Preference(child, n, true)))
                            .collect(Collectors.toList()));

        } catch (BackingStoreException e) {
            new RuntimeException(e);
        }

        return preferences;
    }
}

package dev.jfxde.logic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public final class SettingManager extends Manager {

    public static String SYSTEM_LOCALE = "system.locale";
    public static String SYSTEM_THEME_COLOR = "system.theme.color";

    private Properties userSettings;
    private ObservableList<PropertyDescriptor> settings = FXCollections.observableArrayList(s -> new Observable[] { s.valueProperty() });
    private Set<String> keys = new HashSet<>();

    SettingManager() {
    }

    @Override
    void init() throws Exception {

        Properties defaultSettings = new Properties();
        defaultSettings.load(Files.newBufferedReader(FileManager.DEFAULT_CONF_FILE));
        userSettings = new Properties(defaultSettings);

        if (Files.exists(FileManager.CONF_FILE)) {
            userSettings.load(Files.newBufferedReader(FileManager.CONF_FILE));
        }

        setSettings();

        ResourceManager.setLocale(getLocale());

        setListeners();
    }

    private void setSettings() {
        for (String key : userSettings.stringPropertyNames()) {
            settings.add(new PropertyDescriptor(key, userSettings.getProperty(key)));
            keys.add(key);
            String[] parts = key.split("\\.");

            String partKey = "";

            for (int i = 0; i < parts.length - 1; i++) {
                partKey += partKey.isEmpty() ? parts[i] : "." + parts[i];

                if (!keys.contains(partKey)) {
                    keys.add(partKey);
                    settings.add(new PropertyDescriptor(partKey));
                }
            }
        }
    }

    private void setListeners() {

        settings.addListener((Change<? extends PropertyDescriptor> c) -> {

            while (c.next()) {

                if (c.wasUpdated()) {
                    String oldLocale = getLocale();
                    IntStream.range(c.getFrom(), c.getTo()).mapToObj(i -> c.getList().get(i))
                            .forEach(s -> userSettings.put(s.getKey(), s.getValue()));
                    String newLocale = getLocale();

                    if (!oldLocale.equals(newLocale)) {
                        setLocale(newLocale);
                    }

                    storeSettings();
                }
            }
        });
    }

    private String getLocale() {
        return userSettings.getProperty(SYSTEM_LOCALE);
    }

    private void setLocale(String locale) {

        ResourceManager.setLocale(locale);

        Sys.am().sortApp();
    }

    public String getThemeColor() {
        return userSettings.getProperty(SYSTEM_THEME_COLOR);
    }

    public ObservableList<PropertyDescriptor> getSettings() {

        return settings;
    }

    public ObservableList<PropertyDescriptor> getSystemProperties() {

        ObservableList<PropertyDescriptor> properties = FXCollections.observableArrayList(
                System.getProperties().entrySet().stream().map(PropertyDescriptor::new).collect(Collectors.toList()));
        FXCollections.sort(properties);

        return properties;
    }

    public List<PropertyDescriptor> getSubsettings(PropertyDescriptor propertyDescriptor) {

        FilteredList<PropertyDescriptor> subsettings = new FilteredList<PropertyDescriptor>(settings, p -> p.isSubproperty(propertyDescriptor));

        return subsettings;
    }

    private void storeSettings() {

        Properties userSettingsCopy = new Properties();
        userSettingsCopy.putAll(userSettings);

        Runnable task = new Runnable() {
            public void run() {
                try (BufferedWriter bw = Files.newBufferedWriter(FileManager.CONF_FILE)) {
                    userSettingsCopy.store(bw, "");
                } catch (IOException e) {
                    throw new RuntimeException("Failed storing properties " + FileManager.CONF_FILE, e);
                }
            }
        };

        Sys.tm().executeSequentially(task);
    }
}

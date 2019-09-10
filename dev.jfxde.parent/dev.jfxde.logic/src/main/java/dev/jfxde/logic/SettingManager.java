package dev.jfxde.logic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;

public final class SettingManager extends Manager {

    private Properties userSettings;
    private ObservableList<PropertyDescriptor> settings = FXCollections.observableArrayList( s -> new Observable[] { s.valueProperty() });

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

        settings.setAll(userSettings.entrySet().stream().map(PropertyDescriptor::new).collect(Collectors.toList()));
        FXCollections.sort(settings);

        ResourceManager.setLocale(getLocale());

        setListeners();
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
        return userSettings.getProperty("locale");
    }

    private void setLocale(String locale) {
        ResourceManager.setLocale(locale);
        Sys.am().sortApp();
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

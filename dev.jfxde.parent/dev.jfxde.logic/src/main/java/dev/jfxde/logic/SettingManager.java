package dev.jfxde.logic;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;
import java.util.stream.Collectors;

import dev.jfxde.logic.data.PropertyDescriptor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class SettingManager extends Manager {

	private Properties defaultSettings = new Properties();
	private Properties settings = new Properties(defaultSettings);

	SettingManager() {
	}

	@Override
	void init() throws Exception {

		defaultSettings.load(Files.newBufferedReader(FileManager.DEFAULT_CONF_FILE));
		if (Files.exists(FileManager.CONF_FILE)) {
			settings.load(Files.newBufferedReader(FileManager.CONF_FILE));
		}
		ResourceManager.setLocale(getLocal());
	}

	public String getLocal() {
		String locale = settings.getProperty("locale");

		return locale;
	}

	public void setLocale(String locale) {
		ResourceManager.setLocale(locale);
		Sys.am().sortApp();
		Platform.runLater(() -> {
			settings.setProperty("locale", locale);
			store();
		});
	}

	public ObservableList<PropertyDescriptor> getProperties() {

		ObservableList<PropertyDescriptor> properties = FXCollections.observableArrayList(
				System.getProperties().entrySet().stream().map(PropertyDescriptor::new).collect(Collectors.toList()));
		FXCollections.sort(properties);

		return properties;
	}

	private void store() {
		Properties copy = new Properties();
		copy.putAll(settings);

		Runnable task = new Runnable() {
			public void run() {
				try (BufferedWriter bw = Files.newBufferedWriter(FileManager.CONF_FILE)) {
					copy.store(bw, "");
				} catch (IOException e) {
					throw new RuntimeException("Failed storing properties " + FileManager.CONF_FILE, e);
				}
			}
		};

		Sys.tm().executeSequentially(task);
	}
}

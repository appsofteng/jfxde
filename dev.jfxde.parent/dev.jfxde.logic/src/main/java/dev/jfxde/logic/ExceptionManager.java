package dev.jfxde.logic;

import java.util.logging.Level;
import java.util.logging.Logger;

import dev.jfxde.logic.data.ExceptionDescriptor;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class ExceptionManager extends Manager {

	private final ObservableList<ExceptionDescriptor> exceptionDescriptors = FXCollections.observableArrayList();

	private static final Logger LOGGER = Logger.getLogger(ExceptionManager.class.getName());

	ExceptionManager() {
	}

	@Override
	void init() {
		Thread.setDefaultUncaughtExceptionHandler(this::uncaughtException);
	}

	private void uncaughtException(Thread thread, Throwable throwable) {

		LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
		Platform.runLater(() -> exceptionDescriptors.add(new ExceptionDescriptor(throwable)));
	}

	public void log(Throwable throwable) {
		Platform.runLater(() -> {
			LOGGER.log(Level.SEVERE, throwable.getMessage(), throwable);
			exceptionDescriptors.add(new ExceptionDescriptor(throwable));
		});
	}

	public ObservableList<ExceptionDescriptor> getExceptionDescriptors() {
		return exceptionDescriptors;
	}
}

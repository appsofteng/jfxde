package dev.jfxde.jfxext.util;

import java.util.concurrent.Callable;
import java.util.function.Consumer;

import javafx.concurrent.Task;

public final class TaskUtils {

	private TaskUtils() {

	}

	public static Task<Void> createTask(TRunnable call) {
		Task<Void> task = new Task<>() {

			@Override
			protected Void call() throws Exception {
				call.run();
				return null;
			}
		};

		task.setOnFailed(e -> { throw new RuntimeException(task.getException());} );

		return task;
	}


	public static <T> Task<T> createTask(Callable<T> call) {
		Task<T> task = new Task<>() {

			@Override
			protected T call() throws Exception {
				return call.call();
			}
		};

		task.setOnFailed(e -> { throw new RuntimeException(task.getException());} );

		return task;
	}

	public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded) {
		Task<T> task = new Task<>() {

			@Override
			protected T call() throws Exception {
				return call.call();
			}
		};

		task.setOnSucceeded(e -> {
			onSucceeded.accept(task.getValue());
		});

		task.setOnFailed(e -> { throw new RuntimeException(task.getException());} );

		return task;
	}

	public static <T> Task<T> createTask(Runnable call, Runnable onSucceeded) {
		Task<T> task = new Task<>() {

			@Override
			protected T call() throws Exception {
				call.run();
				return null;
			}
		};

		task.setOnSucceeded(e -> {
			onSucceeded.run();
		});

		task.setOnFailed(e -> { throw new RuntimeException(task.getException());} );

		return task;
	}

	public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded, Runnable onFailed) {
		Task<T> task = new Task<>() {

			@Override
			protected T call() throws Exception {
				return call.call();
			}
		};

		task.setOnSucceeded(e -> {
			onSucceeded.accept(task.getValue());
		});

		task.setOnFailed(e -> {
			onFailed.run();
		});

		return task;
	}

	public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded, Runnable onFailed, Runnable onCancelled) {
		Task<T> task = new Task<>() {

			@Override
			protected T call() throws Exception {
				return call.call();
			}
		};

		task.setOnSucceeded(e -> {
			onSucceeded.accept(task.getValue());
		});

		task.setOnFailed(e -> {
			onFailed.run();
		});

		task.setOnCancelled(e -> {
			onCancelled.run();
		});

		return task;
	}

	public static interface TRunnable {
	    void run() throws Exception;
	}
}

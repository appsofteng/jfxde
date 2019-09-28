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

            protected void failed() {
                throw new RuntimeException(getException());
            };
        };

        return task;
    }

    public static Task<Void> createTask(TRunnable call, Runnable finished) {
        Task<Void> task = new Task<>() {

            @Override
            protected Void call() throws Exception {
                call.run();
                return null;
            }

            protected void succeeded() {
                finished.run();
            };

            protected void cancelled() {
                finished.run();
            };

            protected void failed() {
                finished.run();
            };
        };

        return task;
    }

    public static <T> Task<T> createTask(Callable<T> call) {
        Task<T> task = new Task<>() {

            @Override
            protected T call() throws Exception {
                return call.call();
            }

            protected void failed() {
                throw new RuntimeException(getException());
            };
        };

        return task;
    }

    public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded) {
        Task<T> task = new Task<>() {

            @Override
            protected T call() throws Exception {
                return call.call();
            }

            protected void succeeded() {
                onSucceeded.accept(getValue());
            };

            protected void failed() {
                throw new RuntimeException(getException());
            };
        };

        return task;
    }

    public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded, Runnable onFailed) {
        Task<T> task = new Task<>() {

            @Override
            protected T call() throws Exception {
                return call.call();
            }

            protected void succeeded() {
                onSucceeded.accept(getValue());
            };

            protected void failed() {
                onFailed.run();
            };
        };

        return task;
    }

    public static <T> Task<T> createTask(Callable<T> call, Consumer<T> onSucceeded, Runnable onFailed, Runnable onCancelled) {
        Task<T> task = new Task<>() {

            @Override
            protected T call() throws Exception {
                return call.call();
            }

            protected void succeeded() {
                onSucceeded.accept(getValue());
            };

            protected void cancelled() {
                onCancelled.run();
            };

            protected void failed() {
                onFailed.run();
            };
        };

        return task;
    }

    public static interface TRunnable {
        void run() throws Exception;
    }
}

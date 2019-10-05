package dev.jfxde.api;

import javafx.concurrent.Task;

public interface TaskController {

    <T> Task<T> execute(Task<T> task);

    <T> Task<T> executeSequentially(Task<T> task);

    Task<Void> executeSequentially(TRunnable task);
}

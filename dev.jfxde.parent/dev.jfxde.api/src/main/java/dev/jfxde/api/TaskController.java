package dev.jfxde.api;

import javafx.concurrent.Task;

public interface TaskController {

    <T> Task<T> execute(Task<T> task);
    Task<Void> execute(TRunnable task);

    <T> Task<T> executeSequentially(Task<T> task);
    <T> Task<T> executeSequentially(String queueId, Task<T> task);

    Task<Void> executeSequentially(TRunnable task);
    Task<Void> executeSequentially(String queueId, TRunnable task);
}

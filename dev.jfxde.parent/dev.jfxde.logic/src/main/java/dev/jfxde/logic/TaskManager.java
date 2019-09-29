package dev.jfxde.logic;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.TaskDescriptor;
import javafx.beans.Observable;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;

public final class TaskManager extends Manager {

    private final ExecutorService singleThreadExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService executorService = Executors.newWorkStealingPool();
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    private final ObservableList<TaskDescriptor<? extends Worker<?>>> taskDescriptors = FXCollections
            .observableArrayList((td) -> new Observable[]{td.stateProperty()});
    private final Map<AppDescriptor, List<TaskDescriptor<?>>> appTaskMap = new HashMap<>();

    TaskManager() {

        taskDescriptors.addListener((Change<? extends TaskDescriptor<?>> c) -> {

            while (c.next()) {

                if (c.wasUpdated()) {
                    c.getList().removeIf(td -> td.isFinished());
                }
            }
        });
    }

    public ObservableList<TaskDescriptor<? extends Worker<?>>> getTaskDescriptors() {
        return taskDescriptors;
    }

    public void execute(Task<?> task) {

        executorService.execute(() -> {
            // The threads in the pool are not created by the caller and thus
            // don't inherit the access control context.
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                task.run();
                return null;
            });
        });
    }

    public void executeSequentially(Task<?> task) {
    	singleThreadExecutor.execute(task);
    }

    public void executeSequentially(Runnable task) {

        singleThreadExecutor.execute(task);
    }

    public ScheduledFuture<?> scheduleAtFixedRate​(Runnable task, long initialDelay, long period, TimeUnit unit) {
        ScheduledFuture<?> future = scheduledExecutorService.scheduleAtFixedRate(task, initialDelay, period, unit);
        return future;
    }

    public void execute(AppDescriptor appDescriptor, Task<?> task) {
        addTask(appDescriptor, task);

        executorService.execute(() -> {
            // The threads in the pool are not created by the caller and thus
            // don't inherit the access control context.
            AccessController.doPrivileged((PrivilegedAction<Void>) () -> {
                task.run();
                return null;
            });
        });
    }

    public void removeTasks(AppDescriptor appDescriptor) {
        List<TaskDescriptor<?>> appTasks = appTaskMap.remove(appDescriptor);

        if (appTasks != null) {
            taskDescriptors.removeAll(appTasks);
            appTasks.forEach(t -> t.getTask().cancel());
        }
    }

    public void removeAll(List<TaskDescriptor<?>> descriptors) {
        descriptors.forEach(TaskDescriptor::cancel);

        taskDescriptors.removeAll(descriptors);

        descriptors.forEach(t -> {
            List<TaskDescriptor<?>> appTasks = appTaskMap.get(t.getAppDescriptor());
            appTasks.remove(t);

            if (appTasks.isEmpty()) {
                appTaskMap.remove(t.getAppDescriptor());
            }
        });
    }

    void stop() {
        shutdown(singleThreadExecutor);
        shutdown(executorService);
        shutdown(scheduledExecutorService);
    }

    private void shutdown(ExecutorService executorService) {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
        }
    }

    private <T extends Worker<?>> TaskDescriptor<T> addTask(AppDescriptor appDescriptor, T task) {
        TaskDescriptor<T> taskDescriptor = new TaskDescriptor<>(appDescriptor, task);
        taskDescriptors.add(taskDescriptor);
        List<TaskDescriptor<?>> appTasks = appTaskMap.getOrDefault(appDescriptor, new ArrayList<>());
        appTasks.add(taskDescriptor);
        appTaskMap.put(appDescriptor, appTasks);

        return taskDescriptor;
    }
}

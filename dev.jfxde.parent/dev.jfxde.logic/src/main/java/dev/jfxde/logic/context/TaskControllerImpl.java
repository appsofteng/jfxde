package dev.jfxde.logic.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import dev.jfxde.api.TRunnable;
import dev.jfxde.api.TaskController;
import dev.jfxde.jfx.concurrent.CTask;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import javafx.concurrent.Task;

public class TaskControllerImpl implements TaskController {

    private AppDescriptor appDescriptor;
    private TaskQueue taskQueue;
    private Map<String, TaskQueue> taskQueues = new ConcurrentHashMap<>();

    public TaskControllerImpl(AppDescriptor appDescriptor) {
        this.appDescriptor = appDescriptor;
        this.taskQueue = new TaskQueue(appDescriptor);
    }

    @Override
    public <T> Task<T> execute(Task<T> task) {
        Sys.tm().execute(appDescriptor, task);
        return task;
    }

    @Override
    public Task<Void> execute(TRunnable task) {
        Task<Void> t = CTask.create(() -> task.run());
        Sys.tm().execute(appDescriptor, t);
        return t;
    }

    @Override
    public <T> Task<T> executeSequentially(Task<T> task) {
        taskQueue.add(task);
        return task;
    }

    @Override
    public <T> Task<T> executeSequentially(String queueId, Task<T> task) {
        taskQueues.computeIfAbsent(queueId, k -> new TaskQueue(appDescriptor)).add(task);
        return task;
    }

    @Override
    public Task<Void> executeSequentially(TRunnable task) {
        Task<Void> t = CTask.create(() -> task.run());
        taskQueue.add(t);
        return t;
    }

    @Override
    public Task<Void> executeSequentially(String queueId, TRunnable task) {
        Task<Void> t = CTask.create(() -> task.run());
        taskQueues.computeIfAbsent(queueId, k -> new TaskQueue(appDescriptor)).add(t);
        return t;
    }
}

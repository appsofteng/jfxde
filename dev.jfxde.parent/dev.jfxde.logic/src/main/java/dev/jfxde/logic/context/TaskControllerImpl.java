package dev.jfxde.logic.context;

import dev.jfxde.api.TRunnable;
import dev.jfxde.api.TaskController;
import dev.jfxde.jfxext.util.CTask;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import javafx.concurrent.Task;

public class TaskControllerImpl implements TaskController {

    private AppDescriptor appDescriptor;
    private TaskQueue taskQueue;

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
    public Task<Void> executeSequentially(TRunnable task) {
        Task<Void> t = CTask.create(() -> task.run());
        taskQueue.add(t);
        return t;
    }
}

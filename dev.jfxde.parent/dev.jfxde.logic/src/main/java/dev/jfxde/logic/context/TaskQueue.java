package dev.jfxde.logic.context;

import java.util.ArrayDeque;
import java.util.Queue;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import javafx.concurrent.Task;
import javafx.concurrent.Worker.State;

public class TaskQueue {

    private AppDescriptor appDescriptor;

    public TaskQueue(AppDescriptor appDescriptor) {
        this.appDescriptor = appDescriptor;
    }

    private Queue<Task<?>> queue = new ArrayDeque<>();

    public synchronized void add(Task<?> task) {
        queue.add(task);

        task.stateProperty().addListener((v, o, n) -> {
            if (n == State.SUCCEEDED || n == State.CANCELLED || n == State.FAILED) {
                remove(task);
            }
        });

        if (queue.size() == 1) {

            Sys.tm().execute(appDescriptor, task);
        }
    }

    public synchronized void remove(Task<?> task) {
        queue.remove(task);

        if (queue.size() > 0) {
            Sys.tm().execute(appDescriptor, queue.peek());
        }
    }
}

package dev.jfxde.logic.data;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.concurrent.Worker;
import javafx.concurrent.Worker.State;

public class TaskDescriptor<T extends Worker<?>> {

    private final T task;
    private final AppDescriptor appDescriptor;

    public TaskDescriptor(AppDescriptor appDescriptor, T task) {
        this.appDescriptor = appDescriptor;
        this.task = task;
    }

    public AppDescriptor getAppDescriptor() {
        return appDescriptor;
    }

    public ReadOnlyStringProperty nameProperty() {
        return appDescriptor.nameProperty();
    }

    public ReadOnlyStringProperty titleProperty() {
        return task.titleProperty();
    }

    public T getTask() {
        return task;
    }

    public void cancel() {
        task.cancel();

    }

    public ReadOnlyObjectProperty<State> stateProperty() {
    	return task.stateProperty();
    }

    public boolean isFinished() {
    	return task.getState() == State.SUCCEEDED || task.getState() == State.CANCELLED;
    }
}

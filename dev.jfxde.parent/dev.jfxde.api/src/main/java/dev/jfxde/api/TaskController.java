package dev.jfxde.api;

import javafx.concurrent.Task;

public interface TaskController {

    <T> void execute(Task<T> task);
}

package dev.jfxde.logic.conext;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppController;
import dev.jfxde.api.AppRequest;
import dev.jfxde.api.FileController;
import dev.jfxde.api.ResourceController;
import dev.jfxde.api.TaskController;
import dev.jfxde.logic.data.AppDescriptor;

public class AppContextImpl implements AppContext {

    private AppRequest request;
    private AppDescriptor appDescriptor;
    private FileController fileController;
    private TaskController taskController;
    private AppController appController;

    public AppContextImpl(AppDescriptor appDescriptor) {
        this(appDescriptor, null);
    }

    public AppContextImpl(AppDescriptor appDescriptor, AppRequest request) {
        this.appDescriptor = appDescriptor;
        this.request = request;
        this.fileController = new FileControllerImpl(appDescriptor);
        this.taskController = new TaskControllerImpl(appDescriptor);
        this.appController = new AppControllerImpl(appDescriptor);
    }

    @Override
    public AppRequest getRequest() {
        return request;
    }

    @Override
    public FileController fc() {
        return fileController;
    }

    @Override
    public ResourceController rc() {
        return appDescriptor.getAppProviderDescriptor().getResourceManager();
    }

    @Override
    public TaskController tc() {
        return taskController;
    }

    @Override
    public AppController ac() {
		return appController;
	}
}

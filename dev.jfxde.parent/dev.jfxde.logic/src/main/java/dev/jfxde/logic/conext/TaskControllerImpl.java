package dev.jfxde.logic.conext;

import dev.jfxde.api.TaskController;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import javafx.concurrent.Task;

public class TaskControllerImpl implements TaskController {
	
	private AppDescriptor appDescriptor;
	
	public TaskControllerImpl(AppDescriptor appDescriptor) {
		this.appDescriptor = appDescriptor;
	}

	@Override
	public <T> void execute(Task<T> task) {
		Sys.tm().execute(appDescriptor, task);
	}

}

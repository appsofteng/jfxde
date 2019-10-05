package dev.jfxde.logic.context;

import dev.jfxde.api.AppController;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;

public class AppControllerImpl implements AppController {

    private AppDescriptor appDescriptor;

	public AppControllerImpl(AppDescriptor appDescriptor) {
		this.appDescriptor = appDescriptor;
	}

	@Override
	public void start(String uri) {
		Sys.am().start(appDescriptor, uri);
	}
}

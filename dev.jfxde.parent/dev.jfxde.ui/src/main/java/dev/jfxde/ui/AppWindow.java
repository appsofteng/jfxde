package dev.jfxde.ui;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.Window;

public class AppWindow extends InternalWindow {

	private AppDescriptor appDescriptor;

	public AppWindow(Window window, WindowPane pane) {
		super(window, pane);
		this.appDescriptor = window.getAppDescriptor();

		title.textProperty().bind(appDescriptor.displayProperty());
		title.setGraphic(appDescriptor.getAppProviderDescriptor().getSmallIcon());
        newWindow.setDisable(appDescriptor.isSingleton());

        setContent(appDescriptor.getContent());
	}

	@Override
	protected void onNewWindow() {
		Sys.am().start(appDescriptor.getAppProviderDescriptor());
	}

	@Override
	protected void onClose() {
		Sys.am().stop(appDescriptor);
	}
}

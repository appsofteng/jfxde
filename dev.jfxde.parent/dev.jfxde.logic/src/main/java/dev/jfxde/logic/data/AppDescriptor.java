package dev.jfxde.logic.data;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppScope;
import javafx.application.Platform;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;

public class AppDescriptor implements Comparable<AppDescriptor> {

    private final AppProviderDescriptor appProviderDescriptor;
    private final App app;
    private Window window;
    private StringProperty title = new SimpleStringProperty();
    private ObjectProperty<Node> content = new SimpleObjectProperty<Node>();

	public AppDescriptor(AppProviderDescriptor appProviderDescriptor, App app) {
		this.appProviderDescriptor = appProviderDescriptor;
		this.app = app;
	}

	public void start(AppContext context) throws Exception {
		Node content = app.start(context);

		Platform.runLater(() -> this.content.set(content));
	}

    public AppProviderDescriptor getAppProviderDescriptor() {
        return appProviderDescriptor;
    }

	public App getApp() {
		return app;
	}

	public Window getWindow() {
		return window;
	}

	public void setWindow(Window window) {
		this.window = window;
	}

    public final ReadOnlyStringProperty nameProperty() {
        return appProviderDescriptor.nameProperty();
    }

	public ReadOnlyStringProperty displayProperty() {
	    return title.get() == null ? appProviderDescriptor.nameProperty() : title;
	}

	public String getDisplay() {
		return title.get() == null ? appProviderDescriptor.getName() : title.get();
	}

	public ReadOnlyObjectProperty<Node> contentProperty() {
		return content;
	}

	public boolean isSingleton() {
		return getAppProviderDescriptor().getAppManifest().scope() == AppScope.SINGLETON;
	}

	@Override
	public int compareTo(AppDescriptor o) {
		return getDisplay().compareTo(o.getDisplay());
	}
}

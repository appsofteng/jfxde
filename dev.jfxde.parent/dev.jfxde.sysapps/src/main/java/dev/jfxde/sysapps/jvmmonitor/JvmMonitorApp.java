package dev.jfxde.sysapps.jvmmonitor;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="org.jb.JvmMonitor", name="JVM Monitor", version="1.0.0", altText="M", scope=AppScope.SINGLETON, vendor="JB")
public class JvmMonitorApp implements SystemApp {

	private JvmMonitorContent content;

	@Override
	public Node start(AppContext context) {
		content = new JvmMonitorContent(context);

		return content;
	}

	@Override
	public void stop() {
		content.stop();
	}

}

package dev.jfxde.sysapps.jvmmonitor;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="dev.jfxde.jvmmonitor", name="JVM Monitor", version="1.0.0", altText="M", scope=AppScope.SINGLETON, vendor="JFXDE")
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

package dev.jfxde.sysapps.console;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn = "org.jb.Console", name = "Console", version = "1.0.0", altText = "C", scope = AppScope.SINGLETON, vendor = "JB")
public class ConsoleApp implements SystemApp {

	private ConsoleContent content;

	@Override
	public Node start(AppContext context) {
		return content = new ConsoleContent(context);
	}

	@Override
	public void stop() throws Exception {
		content.dispose();
	}
}

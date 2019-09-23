package dev.jfxde.sysapps.xjshell;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn = "dev.jfxde.xjshell", name = "XJShell", version = "1.0.0", altText = "JS", vendor = "JFXDE")
public class XJShellApp implements SystemApp {

	private XJShellContent content;

	@Override
	public Node start(AppContext context) throws Exception {
		content = new XJShellContent(context);
		return content;
	}

	@Override
	public void stop() throws Exception {
		content.stop();
	}
}

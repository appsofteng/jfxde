package dev.jfxde.sysapps.exceptionlog;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="dev.jfxde.exceptionlog", name="Exception Log", version="1.0.0", altText="EL", scope=AppScope.SINGLETON, vendor="JFXDE")
public class ExceptionLogApp implements SystemApp {

	@Override
	public Node start(AppContext context) {
		return new ExceptionLogContent(context);
	}
}

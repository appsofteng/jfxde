package dev.jfxde.demos.hello;

import java.util.PropertyPermission;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.PermissionEntry;
import javafx.scene.Node;
import javafx.scene.control.Label;

@AppManifest(fqn = "dev.jfxde.hello", name = "hello", version = "1.0.0", vendor = "JFXDE", website = "http://www.duckduckgo.org?q=hello")
@PermissionEntry(type = PropertyPermission.class, target = "user.name", actions = "read")
@PermissionEntry(type = PropertyPermission.class, target = "user.home", actions = "read")
public class HelloApp implements App {

	@Override
	public Node start(AppContext context) {
		String username = "";

		username = System.getProperty("user.name");

		return new Label("Hello " + username + " !");
	}
}

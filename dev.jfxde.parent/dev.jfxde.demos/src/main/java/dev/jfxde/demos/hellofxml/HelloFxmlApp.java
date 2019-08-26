package dev.jfxde.demos.hellofxml;

import java.util.PropertyPermission;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.PermissionEntry;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

@AppManifest(fqn = "org.jb.HelloFxml", name = "Hello FXML", version = "1.0.0", vendor = "JB", website = "http://www.duckduckgo.org?q=hello")
@PermissionEntry(type = PropertyPermission.class, target = "user.name", actions = "read")
@PermissionEntry(type = PropertyPermission.class, target = "user.home", actions = "read")
public class HelloFxmlApp implements App {

	@Override
	public Node start(AppContext context) throws Exception {
	    Parent parent = FXMLLoader.load(getClass().getResource("HelloFxml.fxml"));

		return parent;
	}
}

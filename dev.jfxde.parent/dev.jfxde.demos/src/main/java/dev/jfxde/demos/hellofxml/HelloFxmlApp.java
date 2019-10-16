package dev.jfxde.demos.hellofxml;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;

@AppManifest(fqn = "dev.jfxde.hellofxml", name = "Hello FXML", version = "1.0.0", vendor = "JFXDE", website = "http://www.duckduckgo.org?q=hello")
public class HelloFxmlApp implements App {

	@Override
	public Node start(AppContext context) throws Exception {
	    Parent parent = FXMLLoader.load(getClass().getResource("HelloFxml.fxml"));

		return parent;
	}
}

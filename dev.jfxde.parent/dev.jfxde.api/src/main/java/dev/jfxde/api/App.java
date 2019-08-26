package dev.jfxde.api;

import javafx.scene.Node;

public interface App {

	Node start(AppContext context) throws Exception;

	default void stop() throws Exception {

	}
}

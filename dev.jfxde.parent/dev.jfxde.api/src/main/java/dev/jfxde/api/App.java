package dev.jfxde.api;

import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;

public interface App {

    static final ReadOnlyBooleanProperty STOPPABLE = new SimpleBooleanProperty(true);

	Node start(AppContext context) throws Exception;

	default void stop() throws Exception {

	}

    default ReadOnlyBooleanProperty stoppableProperty() {
        return STOPPABLE;
    }

    default boolean isStoppable() {
        return stoppableProperty().get();
    }
}

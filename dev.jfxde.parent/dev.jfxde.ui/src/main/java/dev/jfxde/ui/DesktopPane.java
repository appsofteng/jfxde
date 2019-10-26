package dev.jfxde.ui;


import dev.jfxde.data.entity.Desktop;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class DesktopPane extends StackPane {

	private Desktop desktop;
	private WindowPane windowPane;
	private ShortcutPane shortcutPane;
	private ModalPane modalPane;
	private BooleanProperty frozen = new SimpleBooleanProperty();

	public DesktopPane(Desktop desktop) {
		this.desktop = desktop;

		windowPane = new WindowPane(desktop);
		shortcutPane = new ShortcutPane(desktop);
		modalPane = new ModalPane(this);

        getChildren().addAll(modalPane, shortcutPane, windowPane);
	}

	ReadOnlyBooleanProperty frozenProperty() {
	    return frozen;
	}

	void setFreeze(boolean value) {
        shortcutPane.setDisable(value);
        windowPane.setDisable(value);
        modalPane.setVisible(value);
        frozen.set(value);
	}

	public Desktop getDesktop() {
		return desktop;
	}

	Pane getModalPane() {
        return modalPane;
    }
}

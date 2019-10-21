package dev.jfxde.ui;

import dev.jfxde.logic.data.Desktop;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;

public class DesktopPane extends StackPane {

	private Desktop desktop;
	private WindowPane windowPane;
	private ShortcutPane shortcutPane;
	private ModalPane modalPane;

	public DesktopPane(Desktop desktop) {
		this.desktop = desktop;

		windowPane = new WindowPane(desktop);
		shortcutPane = new ShortcutPane(desktop);
		modalPane = new ModalPane(this);

        getChildren().addAll(modalPane, shortcutPane, windowPane);
	}

	public Desktop getDesktop() {
		return desktop;
	}

	ShortcutPane getShortcutPane() {
        return shortcutPane;
    }

	WindowPane getWindowPane() {
        return windowPane;
    }

	Pane getModalPane() {
        return modalPane;
    }
}

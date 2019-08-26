package dev.jfxde.ui;

import dev.jfxde.logic.data.Desktop;
import javafx.scene.layout.StackPane;

public class DesktopPane extends StackPane {

	private Desktop desktop;
	private WindowPane windowPane;
	private ShortcutPane shortcutPane;
	
	public DesktopPane(Desktop desktop) {
		this.desktop = desktop;
		
		windowPane = new WindowPane(desktop);
		shortcutPane = new ShortcutPane(desktop);
		        
        getChildren().addAll(shortcutPane, windowPane);
	}
	
	public Desktop getDesktop() {
		return desktop;
	}
}

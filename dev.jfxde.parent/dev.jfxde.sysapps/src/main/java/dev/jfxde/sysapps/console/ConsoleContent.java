package dev.jfxde.sysapps.console;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfxext.control.SplitConsoleView;
import dev.jfxde.logic.Sys;
import javafx.scene.layout.BorderPane;

public class ConsoleContent extends BorderPane {

	private CodeArea codeArea = new CodeArea();

	public ConsoleContent(AppContext context) {

	    SplitConsoleView consoleView = new SplitConsoleView(Sys.cm().getConsoleModel());
		setCenter(consoleView);
	}

    void dispose() {
    	codeArea.dispose();
    }
}

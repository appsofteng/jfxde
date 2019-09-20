package dev.jfxde.sysapps.console;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import dev.jfxde.sysapps.util.ContextMenuBuilder;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.layout.BorderPane;

public class ConsoleContent extends BorderPane {

	private CodeArea codeArea = new CodeArea();

	public ConsoleContent(AppContext context) {
		getStylesheets().add(context.rc().getCss("console"));

		codeArea.setEditable(false);
		codeArea.getStylesheets().add(context.rc().getCss("code-area"));
		setCenter(new VirtualizedScrollPane<>(codeArea));
		setListeners();
		ContextMenuBuilder.get(codeArea, context).copy().clear(e -> Platform.runLater(() -> Sys.cm().clear()));
		CodeAreaUtils.addOutput(codeArea, Sys.cm().getCopyOutputs());
	}

	private void setListeners() {
		Sys.cm().getOutputs().addListener((Change<? extends ConsoleOutput> c) -> {

			while (c.next()) {

				if (c.wasAdded()) {
					List<? extends ConsoleOutput> added = new ArrayList<>(c.getAddedSubList());
					Platform.runLater(() -> {
						CodeAreaUtils.addOutput(codeArea, added);
					});
				} else if (c.wasRemoved()) {
					String removed = c.getRemoved().stream().map(ConsoleOutput::getText).collect(Collectors.joining());
					Platform.runLater(() -> {
						codeArea.deleteText(0, Math.min(removed.length(), codeArea.getLength()));
						codeArea.moveTo(codeArea.getLength());
						codeArea.requestFollowCaret();
					});
				}
			}
		});
	}

    void dispose() {
    	codeArea.dispose();
    }
}

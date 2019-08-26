package dev.jfxde.sysapps.console;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.ConsoleOutput;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener.Change;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;

public class ConsoleContent extends BorderPane {

	private AppContext context;
	private CodeArea codeArea = new CodeArea();

	public ConsoleContent(AppContext context) {
		this.context = context;
		getStylesheets().add(context.rc().getCss("console"));

		codeArea.setEditable(false);
		codeArea.getStylesheets().add(context.rc().getCss("area"));
		setCenter(new VirtualizedScrollPane<>(codeArea));
		setListeners();
		setContextMenu();
		addOutput(Sys.cm().getCopyOutputs());
	}

	private void setListeners() {
		Sys.cm().getOutputs().addListener((Change<? extends ConsoleOutput> c) -> {

			while (c.next()) {

				if (c.wasAdded()) {
					List<? extends ConsoleOutput> added = new ArrayList<>(c.getAddedSubList());
					Platform.runLater(() -> {
						addOutput(added);
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

	private void setContextMenu() {
		ContextMenu contextMenu = new ContextMenu();

		MenuItem copy = new MenuItem();
		copy.textProperty().bind(context.rc().getTextBinding("copy"));
		copy.setOnAction(e -> codeArea.copy());
		copy.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getSelection().getLength() == 0, codeArea.selectionProperty()));

		MenuItem clear = new MenuItem();
		clear.textProperty().bind(context.rc().getTextBinding("clear"));
		clear.setOnAction(e -> Platform.runLater(() -> Sys.cm().clear()));
		clear.disableProperty().bind(Bindings.createBooleanBinding(() -> codeArea.getLength() == 0, codeArea.lengthProperty()));

		contextMenu.getItems().addAll(copy, clear);
		codeArea.setContextMenu(contextMenu);
	}

	private void addOutput(List<? extends ConsoleOutput> outputs) {

		if (outputs.isEmpty()) {
			return;
		}

		String text = "";
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

		for (ConsoleOutput co : outputs) {
			text += co.getText();
			spansBuilder.add(Collections.singleton("jd-console-" + co.getType().name().toLowerCase()),
					co.getText().length());
		}

		int from = codeArea.getLength();
		codeArea.appendText(text);
		StyleSpans<Collection<String>> styleSpans = spansBuilder.create();
		codeArea.setStyleSpans(from, styleSpans);
		codeArea.moveTo(codeArea.getLength());
		codeArea.requestFollowCaret();
	}

    void dispose() {
    	codeArea.dispose();
    }
}

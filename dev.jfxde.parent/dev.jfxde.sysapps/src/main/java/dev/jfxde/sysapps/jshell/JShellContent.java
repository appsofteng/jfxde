package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.ConsoleManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import javafx.application.Platform;
import javafx.collections.ListChangeListener.Change;
import javafx.concurrent.Task;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.BorderPane;
import jdk.jshell.JShell;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;

public class JShellContent extends BorderPane {

	private CodeArea inputArea = new CodeArea();
	private CodeArea outputArea = new CodeArea();
	private JShell jshell;
	private ConsoleManager consoleManager = new ConsoleManager(false);
	private List<String> history = new ArrayList<>();
	private int historyIndex;

	public JShellContent(AppContext context) {
		getStylesheets().add(context.rc().getCss("console"));
		outputArea.setEditable(false);
		outputArea.getStylesheets().add(context.rc().getCss("code-area"));
		outputArea.setFocusTraversable(false);

		inputArea.requestFocus();
		inputArea.getStylesheets().add(context.rc().getCss("code-area"));
		inputArea.setWrapText(true);

		setCenter(new VirtualizedScrollPane<>(outputArea));
		setBottom(new VirtualizedScrollPane<>(inputArea));

		setListeners();

		jshell = JShell.builder().out(consoleManager.getCout()).err(consoleManager.getCerr()).build();
	}

	private void setListeners() {

		consoleManager.getOutputs().addListener((Change<? extends ConsoleOutput> c) -> {

			while (c.next()) {

				if (c.wasAdded()) {
					List<? extends ConsoleOutput> added = new ArrayList<>(c.getAddedSubList());
					Platform.runLater(() -> {
						CodeAreaUtils.addOutput(outputArea, added);
					});
				}
			}
		});

		outputArea.selectedTextProperty().addListener((v,o,n) -> {
			Clipboard clipboard = Clipboard.getSystemClipboard();
			ClipboardContent content = new ClipboardContent();
			content.putString(outputArea.getSelectedText());
			clipboard.setContent(content);
		});

		outputArea.focusedProperty().addListener((v, o, n) -> {
			if (n) {
				inputArea.requestFocus();
			}
		});

		inputArea.setOnKeyPressed(e -> {

			if (e.getCode() == KeyCode.ENTER && e.isShiftDown()) {
				enter();
			} else if (e.getCode() == KeyCode.UP && e.isControlDown()) {
				historyUp(e);
			} else if (e.getCode() == KeyCode.DOWN && e.isControlDown()) {
				historyDown(e);
			}
		});
	}

	private void enter() {
		String input = inputArea.getText();
		inputArea.replaceText("");

		update(input);

		if (input.isBlank()) {
			return;
		}

		history.add(input);
		historyIndex = history.size();

		Task<Void> task = new Task<>() {

			@Override
			protected Void call() throws Exception {

				SourceCodeAnalysis sourceAnalysis = jshell.sourceCodeAnalysis();
				SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(input);

				String source = info.source();

				while (!source.isEmpty()) {

					List<SnippetEvent> snippetEvents = jshell.eval(source);
					snippetEvents.forEach(e -> updateLater(SnippetOutput.get(jshell, e)));

					info = sourceAnalysis.analyzeCompletion(info.remaining());
					source = info.source();
				}

				return null;
			}

			private void updateLater(SnippetOutput snippetOuput) {
				String output = snippetOuput.build();

				Platform.runLater(() -> {
					if (snippetOuput.isError()) {
						updateError(output);
					} else {
						update(output);
					}
				});
			}
		};

		Sys.tm().executeSequentially(task);
	}

	private void historyUp(KeyEvent e) {

		if (historyIndex > 0 && historyIndex <= history.size()) {
			historyIndex--;
			String item = history.get(historyIndex);
			inputArea.replaceText(item);
		}
	}

	private void historyDown(KeyEvent e) {

		if (historyIndex >= 0 && historyIndex < history.size() - 1) {
			historyIndex++;
			String item = history.get(historyIndex);
			inputArea.replaceText(item);
		} else {
			inputArea.replaceText("");
			historyIndex = history.size();
		}
	}

	private void updateError(String output) {
		StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
		spansBuilder.add(Collections.singleton("jd-console-error"), output.length());

		int from = outputArea.getLength();
		outputArea.appendText(output + "\n");

		StyleSpans<Collection<String>> styleSpans = spansBuilder.create();

		outputArea.setStyleSpans(from, styleSpans);
		outputArea.moveTo(outputArea.getLength());
		outputArea.requestFollowCaret();
	}

	private void update(String output) {
		outputArea.appendText(output + "\n");
		outputArea.moveTo(outputArea.getLength());
		outputArea.requestFollowCaret();
	}

	@Override
	public void requestFocus() {
		super.requestFocus();
		inputArea.requestFocus();
	}

	public void stop() {
		inputArea.dispose();
		outputArea.dispose();
		jshell.close();
	}
}

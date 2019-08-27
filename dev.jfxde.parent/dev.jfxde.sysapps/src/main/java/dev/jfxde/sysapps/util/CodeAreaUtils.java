package dev.jfxde.sysapps.util;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import dev.jfxde.logic.data.ConsoleOutput;

public final class CodeAreaUtils {

	private CodeAreaUtils() {
	}

	public static void addOutput(CodeArea codeArea, List<? extends ConsoleOutput> outputs) {

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
}

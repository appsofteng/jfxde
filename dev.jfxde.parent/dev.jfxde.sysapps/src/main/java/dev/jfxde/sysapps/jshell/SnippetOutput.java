package dev.jfxde.sysapps.jshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import jdk.jshell.EvalException;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.VarSnippet;

public class SnippetOutput {

	private JShell jshell;
	private SnippetEvent event;
	private boolean error;

	private SnippetOutput(JShell jshell, SnippetEvent event) {
		this.jshell = jshell;
		this.event = event;
	}

	public static SnippetOutput get(JShell jshell, SnippetEvent event) {
		return new SnippetOutput(jshell, event);
	}

	public boolean isError() {
		return error;
	}

	public String build() {

		error = event.status() == Status.REJECTED || event.exception() != null;
		StringBuilder sb = new StringBuilder();
		Snippet snippet = event.snippet();

		if (event.exception() != null) {

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			event.exception().printStackTrace(new PrintStream(out));
			String msg = out.toString();

			if (event.exception() instanceof EvalException) {
				EvalException e = (EvalException) event.exception();
				msg = msg.replace(event.exception().getClass().getName(), e.getExceptionClassName());
			}

			sb.append("Exception ").append(msg);

		} else if (event.status() == Status.REJECTED) {
			jshell.diagnostics(event.snippet()).forEach(d -> {
				if (d.isError()) {
					sb.append("Error:\n");
				}
				sb.append(d.getMessage(null)).append("\n");
				sb.append(event.snippet().source()).append("\n");

				String underscore = LongStream.range(0, d.getEndPosition())
						.mapToObj(p -> p >= 0 && p < d.getStartPosition() ? " "
								: p > d.getStartPosition() && p < d.getEndPosition() - 1 ? "-" : "^")
						.collect(Collectors.joining());

				sb.append(underscore);
				sb.append("\n");

			});
		} else if (event.value() != null) {
			if (snippet.kind() == jdk.jshell.Snippet.Kind.EXPRESSION) {
				sb.append(((ExpressionSnippet) snippet).name() + " ==> " + event.value());
			} else if (snippet.kind() == jdk.jshell.Snippet.Kind.VAR) {
				sb.append(((VarSnippet) snippet).name() + " ==> " + event.value());
			}
		}

		return sb.toString().trim();
	}
}

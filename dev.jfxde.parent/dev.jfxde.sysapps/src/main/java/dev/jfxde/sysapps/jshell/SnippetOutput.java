package dev.jfxde.sysapps.jshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.util.Tuple2;

import dev.jfxde.api.AppContext;
import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import javafx.application.Platform;
import jdk.jshell.EvalException;
import jdk.jshell.JShell;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.VarSnippet;

public class SnippetOutput extends JShellOutput {

    SnippetOutput(AppContext context, JShell jshell, CodeArea outputArea) {
        super(context, jshell, outputArea);
    }

    @Override
    public void output(String input) {

        SourceCodeAnalysis sourceAnalysis = jshell.sourceCodeAnalysis();
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(input);

        String source = info.source();

        List<ConsoleOutput> outputs = new ArrayList<>();

        while (!source.isEmpty()) {

            List<SnippetEvent> snippetEvents = jshell.eval(source);
            snippetEvents.forEach(e -> outputs.add(getOutput(e)));

            info = sourceAnalysis.analyzeCompletion(info.remaining());
            source = info.source();
        }

        outputs.add(new ConsoleOutput("\n"));

        Platform.runLater(() -> {
            if (!outputArea.getText().endsWith("\n")) {
                outputs.add(0, new ConsoleOutput("\n"));
            }

            CodeAreaUtils.addOutput(outputArea, outputs);
        });
    }

    private ConsoleOutput getOutput(SnippetEvent event) {

        String message = "";
        Type type = Type.COMMENT;

        if (event.exception() != null) {
            type = Type.ERROR;
            message = getExceptionMessage(event);
        } else if (event.status() == Status.REJECTED) {
            type = Type.ERROR;
            message = getRejectedMessage(event);
        } else {
            message = getSuccessMessage(event);
        }

        message = message.strip();

        if (!message.isBlank()) {

            message += "\n";
        }

        ConsoleOutput o = new ConsoleOutput(message, type);

        return o;
    }

    private String getExceptionMessage(SnippetEvent event) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        event.exception().printStackTrace(new PrintStream(out));
        String msg = out.toString();

        if (event.exception() instanceof EvalException) {
            EvalException e = (EvalException) event.exception();
            msg = msg.replace(event.exception().getClass().getName(), e.getExceptionClassName());
        }

        msg = "Exception " + msg.replace("\r", "");

        return msg;
    }

    private String getRejectedMessage(SnippetEvent event) {
        StringBuilder sb = new StringBuilder();

        jshell.diagnostics(event.snippet()).forEach(d -> {
            if (d.isError()) {
                sb.append("Error:\n");
            }
            sb.append(d.getMessage(null)).append("\n");

            Tuple2<String, Integer> line = SnippetUtils.getLine(event.snippet().source(), (int) d.getStartPosition(), (int) d.getEndPosition());
            sb.append(line._1).append("\n");

            String underscore = LongStream
                    .range(0,
                            d.getEndPosition() - line._2)
                    .mapToObj(p -> p >= 0 && p < d.getStartPosition() - line._2 ? " "
                            : p > d.getStartPosition() - line._2 && p < d.getEndPosition() - 1 - line._2 ? "-"
                                    : "^")
                    .collect(Collectors.joining());

            sb.append(underscore).append("\n");
        });

        return sb.toString();
    }

    private String getSuccessMessage(SnippetEvent event) {

        if (event.previousStatus() == event.status()) {
            return "";
        }

        String msg = "";

        if (event.snippet().kind() == Kind.EXPRESSION) {
            msg = "";
        } else if (event.previousStatus() == Status.NONEXISTENT) {
            msg = "created";
        } else if (event.status() == Status.OVERWRITTEN) {
            if (event.causeSnippet().subKind() == event.snippet().subKind()) {
                msg = "modified";
            } else {
                msg = "replaced";
            }
        }

        String value = event.value();

        if (value == null && event.causeSnippet() != null && event.causeSnippet() instanceof VarSnippet) {
            value = jshell.varValue((VarSnippet) event.causeSnippet());
        }

        msg += SnippetUtils.toString(event.snippet(), value);

        return msg;
    }
}

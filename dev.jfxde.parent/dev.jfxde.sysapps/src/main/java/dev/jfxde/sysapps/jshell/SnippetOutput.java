package dev.jfxde.sysapps.jshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import jdk.jshell.EvalException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.VarSnippet;

public class SnippetOutput extends JShellOutput {

    SnippetOutput(JShellContent jshellContent) {
        super(jshellContent);
    }

    @Override
    public void process(String input) {

        SourceCodeAnalysis sourceAnalysis = jshell.sourceCodeAnalysis();

        String[] lines = input.split("\n");
        StringBuffer sb = new StringBuffer();

        for (String line : lines) {
            sb.append(line).append("\n");
            CompletionInfo info = sourceAnalysis.analyzeCompletion(sb.toString());

            if (info.completeness() == Completeness.CONSIDERED_INCOMPLETE ||
                    info.completeness() == Completeness.DEFINITELY_INCOMPLETE) {
                continue;
            }  else if (info.completeness() == Completeness.EMPTY) {
                sb.delete(0, sb.length());
                continue;
            } else if (info.completeness() == Completeness.UNKNOWN) {
                consoleModel.addNewLineOutput(new TextStyleSpans("unknown", ConsoleModel.ERROR_STYLE));
                sb.delete(0, sb.length());
                continue;
            }

            String source = info.source();
            sb.delete(0, sb.length()).append(info.remaining());
            List<SnippetEvent> snippetEvents = jshell.eval(source);
            snippetEvents.forEach(e -> consoleModel.addNewLineOutput(getOutput(e)));
        }

        consoleModel.getOutput().add(new TextStyleSpans("\n"));
    }

    public void output(List<Snippet> snippets) {

        if (snippets.isEmpty()) {
            consoleModel.addNewLineOutput(new TextStyleSpans(context.rc().getString("noSuchSnippet"), ConsoleModel.COMMENT_STYLE));
        }

        for (Snippet snippet : snippets) {
            consoleModel.addNewLineOutput(new TextStyleSpans(snippet.source()));
            List<SnippetEvent> snippetEvents = jshell.eval(snippet.source());
            snippetEvents.forEach(e -> consoleModel.addNewLineOutput(getOutput(e)));
        }

        consoleModel.getOutput().add(new TextStyleSpans("\n"));
    }

    private TextStyleSpans getOutput(SnippetEvent event) {

        String message = "";
        String type = ConsoleModel.COMMENT_STYLE;

        if (event.exception() != null) {
            type = ConsoleModel.ERROR_STYLE;
            message = getExceptionMessage(event);
        } else if (event.status() == Status.REJECTED) {
            type = ConsoleModel.ERROR_STYLE;
            message = getRejectedMessage(event);
        } else {
            message = getSuccessMessage(event);
        }

        message = message.strip();

        TextStyleSpans o = new TextStyleSpans(message, type);

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

            String errorLine = SnippetUtils.getErrorLine(event.snippet().source(), (int) d.getStartPosition(), (int) d.getEndPosition());

            sb.append(errorLine).append("\n");
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

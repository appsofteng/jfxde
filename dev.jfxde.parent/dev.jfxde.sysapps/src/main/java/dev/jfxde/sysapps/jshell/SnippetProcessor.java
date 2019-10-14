package dev.jfxde.sysapps.jshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import jdk.jshell.DeclarationSnippet;
import jdk.jshell.EvalException;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.SourceCodeAnalysis.Completeness;
import jdk.jshell.SourceCodeAnalysis.CompletionInfo;
import jdk.jshell.VarSnippet;

public class SnippetProcessor extends Processor {

    SnippetProcessor(Session session) {
        super(session);
    }

    @Override
    public void process(String input) {
        getSnippetEvents(input);
    }

    public List<SnippetEvent> getSnippetEvents(String input) {

        SourceCodeAnalysis sourceAnalysis = session.getJshell().sourceCodeAnalysis();

        String[] lines = input.split("\n");
        StringBuffer sb = new StringBuffer();

        List<SnippetEvent> allSnippetEvents = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            sb.append(lines[i]).append("\n");
            CompletionInfo info = sourceAnalysis.analyzeCompletion(sb.toString());

            if (info.completeness() == Completeness.CONSIDERED_INCOMPLETE) {
                continue;
            } else if (info.completeness() == Completeness.DEFINITELY_INCOMPLETE) {
                if (i == lines.length - 1) {
                    session.getFeedback().normaln(session.getContext().rc().getString("definitelyIncomplete") + "  " + sb.toString().strip(),
                            ConsoleModel.ERROR_STYLE);
                }
                continue;
            } else if (info.completeness() == Completeness.EMPTY) {
                sb.delete(0, sb.length());
                continue;
            } else if (info.completeness() == Completeness.UNKNOWN) {
                session.getFeedback().normaln(session.getContext().rc().getString("unknown") + "  " + sb.toString().strip(),
                        ConsoleModel.ERROR_STYLE);
                sb.delete(0, sb.length());
                continue;
            }

            String source = info.source();
            sb.delete(0, sb.length()).append(info.remaining());
            List<SnippetEvent> snippetEvents = session.getJshell().eval(source);
            allSnippetEvents.addAll(snippetEvents);
            snippetEvents.forEach(e -> session.getFeedback().normal(getOutput(e)));
        }

        return allSnippetEvents;
    }

    public void process(List<Snippet> snippets) {

        if (snippets.isEmpty()) {
            session.getFeedback().normaln(session.getContext().rc().getString("noSuchSnippet"), ConsoleModel.COMMENT_STYLE);
        }

        for (Snippet snippet : snippets) {
            session.getFeedback().normaln(snippet.source().strip());
            List<SnippetEvent> snippetEvents = session.getJshell().eval(snippet.source());
            snippetEvents.forEach(e -> session.getFeedback().normal(getOutput(e)));
        }
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

        message = message.strip() + "\n";

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

        msg = session.getContext().rc().getString("exception") + " " + msg.replace("\r", "");

        return msg;
    }

    private String getRejectedMessage(SnippetEvent event) {
        StringBuilder sb = new StringBuilder();

        session.getJshell().diagnostics(event.snippet()).forEach(d -> {
            if (d.isError()) {
                sb.append(session.getContext().rc().getString("error") + ":\n");
            }
            sb.append(d.getMessage(null)).append("\n");

            String errorLine = SnippetUtils.getErrorLine(event.snippet().source(), (int) d.getStartPosition(), (int) d.getEndPosition());

            sb.append(errorLine).append("\n");
        });

        return sb.toString();
    }

    private String getSuccessMessage(SnippetEvent event) {

        if (event.previousStatus() == event.status() || event.previousStatus() == Status.RECOVERABLE_DEFINED
                || event.previousStatus() == Status.RECOVERABLE_NOT_DEFINED) {
            return "";
        }

        String msg = "";

        if (event.snippet().kind() == Kind.EXPRESSION) {
            msg = "";
        } else if (event.previousStatus() == Status.NONEXISTENT) {
            msg = session.getContext().rc().getString("created");
        } else if (event.status() == Status.OVERWRITTEN) {
            if (event.causeSnippet().subKind() == event.snippet().subKind()) {
                msg = session.getContext().rc().getString("modified");
            } else {
                msg = session.getContext().rc().getString("replaced");
            }
        }

        String dependency = "";
        if (event.status() == Status.RECOVERABLE_DEFINED || event.status() == Status.RECOVERABLE_NOT_DEFINED) {
            Snippet snippet = event.snippet();

            if (snippet != null && snippet instanceof DeclarationSnippet) {
                String dependencies = session.getJshell().unresolvedDependencies((DeclarationSnippet)snippet).collect(Collectors.joining(", "));
                dependency = ", " + session.getContext().rc().getString("undeclared", dependencies);
            }
        }

        String value = event.value();
        Snippet snippet = event.snippet();

        if (event.causeSnippet() != null) {
            snippet = event.causeSnippet();
            if (snippet instanceof VarSnippet && session.getJshell().status(snippet) == Status.VALID) {
                value = session.getJshell().varValue((VarSnippet) event.causeSnippet());
            }
        }

        msg += SnippetUtils.toString(snippet, value, session.getJshell()).stripTrailing() + dependency + "\n";

        return msg;
    }
}

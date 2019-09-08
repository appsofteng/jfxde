package dev.jfxde.sysapps.jshell;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import org.fxmisc.richtext.CodeArea;
import org.reactfx.util.Tuple2;
import org.reactfx.util.Tuples;

import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.EvalException;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Kind;
import jdk.jshell.Snippet.Status;
import jdk.jshell.SnippetEvent;
import jdk.jshell.SourceCodeAnalysis;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;

public class SnippetOutput extends JShellOutput {

    SnippetOutput(JShell jshell, CodeArea outputArea) {
       super(jshell, outputArea);
    }

    @Override
    public void output(String input) {

        SourceCodeAnalysis sourceAnalysis = jshell.sourceCodeAnalysis();
        SourceCodeAnalysis.CompletionInfo info = sourceAnalysis.analyzeCompletion(input);

        String source = info.source();

        while (!source.isEmpty()) {

            List<SnippetEvent> snippetEvents = jshell.eval(source);
            snippetEvents.forEach(e -> CodeAreaUtils.addOutputLater(outputArea, getOutputs(e)));

            info = sourceAnalysis.analyzeCompletion(info.remaining());
            source = info.source();
        }

        CodeAreaUtils.addOutputLater(outputArea, "\n");
    }

    private List<ConsoleOutput> getOutputs(SnippetEvent event) {

        List<ConsoleOutput> outputs = new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        Snippet snippet = event.snippet();
        Type type = Type.NORMAL;

        if (event.exception() != null) {

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            event.exception().printStackTrace(new PrintStream(out));
            String msg = out.toString();

            if (event.exception() instanceof EvalException) {
                EvalException e = (EvalException) event.exception();
                msg = msg.replace(event.exception().getClass().getName(), e.getExceptionClassName());
            }

            msg = msg.replace("\r", "");
            sb.append("Exception ").append(msg);

            type = Type.ERROR;

        } else if (event.status() == Status.REJECTED) {
            jshell.diagnostics(event.snippet()).forEach(d -> {
                if (d.isError()) {
                    sb.append("Error:\n");
                }
                sb.append(d.getMessage(null)).append("\n");

                Tuple2<String, Integer> line = getLine(snippet.source(), (int) d.getStartPosition(),
                        (int) d.getEndPosition());
                sb.append(line._1).append("\n");

                String underscore = LongStream
                        .range(0,
                                d.getEndPosition() - line._2)
                        .mapToObj(p -> p >= 0 && p < d.getStartPosition() - line._2 ? " "
                                : p > d.getStartPosition() - line._2 && p < d.getEndPosition() - 1 - line._2 ? "-"
                                        : "^")
                        .collect(Collectors.joining());

                sb.append(underscore);
                sb.append("\n");

            });

            type = Type.ERROR;
        } else if (event.value() != null) {
            if (snippet.kind() == Kind.EXPRESSION) {
                sb.append(((ExpressionSnippet) snippet).name() + " ==> " + event.value());
            } else if (snippet.kind() == Kind.VAR) {
                sb.append(((VarSnippet) snippet).name() + " ==> " + event.value());
            }
        } else if (snippet.kind() == Kind.METHOD) {
            MethodSnippet methodSnippet = ((MethodSnippet) snippet);
            if (event.previousStatus() == Status.NONEXISTENT) {
                sb.append("created method " + methodSnippet.name() + "(" + methodSnippet.parameterTypes() + ")");
            } else if (event.status() == Status.OVERWRITTEN) {
                sb.append("modified method " + methodSnippet.name() + "(" + methodSnippet.parameterTypes() + ")");
            }

            type = Type.COMMENT;
        } else if (snippet.kind() == Kind.TYPE_DECL) {
            TypeDeclSnippet typeSnippet = ((TypeDeclSnippet) snippet);
            if (event.previousStatus() == Status.NONEXISTENT) {
                sb.append("created " + getSubkind(typeSnippet) + " " + typeSnippet.name());
            } else if (event.status() == Status.OVERWRITTEN) {
                if (event.causeSnippet().subKind() == typeSnippet.subKind()) {
                    sb.append("modified " + getSubkind(typeSnippet) + " " + typeSnippet.name());
                } else {
                    sb.append("replaced " + getSubkind(typeSnippet) + " " + typeSnippet.name());
                }
            }

            type = Type.COMMENT;
        }

        String output = sb.toString().trim();

        if (!output.isBlank()) {
            output += "\n";
        }

        outputs.add(new ConsoleOutput(output, type));

        return outputs;
    }

    private String getSubkind(Snippet snippet) {
        String name = snippet.subKind().name();
        String subkind = name.substring(0, name.indexOf("_"));

        return subkind;
    }

    private Tuple2<String, Integer> getLine(String text, int start, int end) {
        int lineStart = text.lastIndexOf("\n", start) + 1;
        int lineEnd = text.indexOf("\n", end);
        lineEnd = lineEnd == -1 ? text.length() : lineEnd;
        String line = text.substring(lineStart, lineEnd);

        Tuple2<String, Integer> result = Tuples.t(line, lineStart);

        return result;
    }
}

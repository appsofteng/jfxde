package dev.jfxde.sysapps.jshell;

import java.util.stream.Collectors;

import jdk.jshell.ErroneousSnippet;
import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.PersistentSnippet;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;
import jdk.jshell.StatementSnippet;
import jdk.jshell.TypeDeclSnippet;
import jdk.jshell.VarSnippet;

public final class SnippetUtils {

    private SnippetUtils() {
    }

    public static String getName(Snippet snippet) {

        String name = "";

        if (snippet instanceof ExpressionSnippet) {
            name = ((ExpressionSnippet)snippet).name();
        } else if (snippet instanceof PersistentSnippet) {
            name = ((PersistentSnippet)snippet).name();
        }

        if (name == null) {
            name = "";
        }

        return name;
    }

    public static String getSubkind(Snippet snippet) {
        String name = snippet.subKind().name();
        String subkind = name.substring(0, name.indexOf("_")).toLowerCase();

        return subkind;
    }

    public static String getErrorLine(String text, int errorStart, int errorEnd) {

        int lineStart = text.lastIndexOf("\n", errorStart) + 1;
        int lineEnd = text.indexOf("\n", errorEnd);
        lineEnd = lineEnd == -1 ? text.length() : lineEnd;
        lineStart = lineStart > lineEnd ? text.lastIndexOf("\n", errorStart - 1) + 1 : lineStart;


        StringBuffer sb = new StringBuffer();
        sb.append(text.substring(lineStart, lineEnd)).append("\n");

        for (int i = lineStart; i <= errorEnd; i++) {

            if (i < errorStart) {
                sb.append(" ");
            } else if (i == errorStart || i == errorEnd - 1) {
                sb.append("^");
            } else if (i > errorStart && i < errorEnd - 1) {
                sb.append("-");
            }
        }

        return sb.toString();
    }

    public static String toString(Snippet snippet, JShell jshell) {

        String value = "";

        if (snippet instanceof VarSnippet && jshell.status(snippet) == Status.VALID) {
            value = jshell.varValue((VarSnippet)snippet);
        }

        return toString(snippet, value, jshell.status(snippet).toString());
    }

    public static String toString(Snippet snippet, String value, JShell jshell) {
        return toString(snippet, value, jshell.status(snippet).toString());
    }

    private static String toString(Snippet snippet, String value, String status) {

        String output = String.format("%4s : ", snippet.id());

        if (snippet instanceof ImportSnippet) {
            output += toString((ImportSnippet)snippet);
        } else if (snippet instanceof MethodSnippet) {
            output += toString((MethodSnippet)snippet);
        } else if (snippet instanceof TypeDeclSnippet) {
            output += toString((TypeDeclSnippet)snippet);
        } else if (snippet instanceof VarSnippet) {
            output += toString((VarSnippet)snippet, value);
        } else if (snippet instanceof ExpressionSnippet) {
            output += toString((ExpressionSnippet)snippet, value);
        } else if (snippet instanceof StatementSnippet) {
            output += toString((StatementSnippet)snippet);
        } else if (snippet instanceof ErroneousSnippet) {
            output += toString((ErroneousSnippet)snippet);
        }

        output +=  " " + status + "\n";

        return output;
    }

    public static String toString(ImportSnippet snippet) {

        return "import " + (snippet.isStatic() ? "static " : "") + snippet.fullname();
    }

    public static String toString(MethodSnippet snippet) {

        return snippet.signature().substring(snippet.signature().indexOf(")") + 1) + " " + snippet.name() + "(" + snippet.parameterTypes() + ")";
    }

    public static String toString(TypeDeclSnippet snippet) {

        return SnippetUtils.getSubkind(snippet) + " " + snippet.name();
    }

    public static String toString(VarSnippet snippet, String value) {

        return snippet.source().strip() + " => " + snippet.typeName() + " " + snippet.name() + " = " + value;
    }

    public static String toString(ExpressionSnippet snippet, String value) {

        return snippet.source().strip() + " => " + snippet.typeName() + " " + snippet.name() + " = " + value;
    }

    public static String toString(StatementSnippet snippet) {

        return snippet.source().strip().lines().map(String::strip).collect(Collectors.joining());
    }

    public static String toString(ErroneousSnippet snippet) {

        return snippet.source().strip().lines().map(String::strip).collect(Collectors.joining());
    }
}

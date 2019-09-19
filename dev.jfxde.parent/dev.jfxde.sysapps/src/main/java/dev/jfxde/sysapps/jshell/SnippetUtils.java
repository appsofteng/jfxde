package dev.jfxde.sysapps.jshell;

import org.reactfx.util.Tuple2;
import org.reactfx.util.Tuples;

import jdk.jshell.ExpressionSnippet;
import jdk.jshell.ImportSnippet;
import jdk.jshell.JShell;
import jdk.jshell.MethodSnippet;
import jdk.jshell.PersistentSnippet;
import jdk.jshell.Snippet;
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

        return name;
    }

    public static String getSubkind(Snippet snippet) {
        String name = snippet.subKind().name();
        String subkind = name.substring(0, name.indexOf("_")).toLowerCase();

        return subkind;
    }

    public static Tuple2<String, Integer> getLine(String text, int start, int end) {
        int lineStart = text.lastIndexOf("\n", start) + 1;
        int lineEnd = text.indexOf("\n", end);
        lineEnd = lineEnd == -1 ? text.length() : lineEnd;
        String line = text.substring(lineStart, lineEnd);

        Tuple2<String, Integer> result = Tuples.t(line, lineStart);

        return result;
    }

    public static String toString(Snippet snippet, JShell jshell) {

        String output = String.format("%4s : ", snippet.id());

        if (snippet instanceof ImportSnippet) {
            output += toString((ImportSnippet)snippet);
        } else if (snippet instanceof MethodSnippet) {
            output += toString((MethodSnippet)snippet);
        } else if (snippet instanceof TypeDeclSnippet) {
            output += toString((TypeDeclSnippet)snippet);
        } else if (snippet instanceof VarSnippet) {
            output += toString((VarSnippet)snippet, jshell.varValue((VarSnippet)snippet));
        }

        return output;
    }

    public static String toString(ImportSnippet snippet) {

        return "import " + (snippet.isStatic() ? "static " : "") + snippet.fullname() + "\n";

    }

    public static String toString(MethodSnippet snippet) {

        return snippet.signature().substring(snippet.signature().indexOf(")") + 1) + " " + snippet.name() + "(" + snippet.parameterTypes() + ")\n";
    }

    public static String toString(TypeDeclSnippet snippet) {

        return SnippetUtils.getSubkind(snippet) + " " + snippet.name() + "\n";
    }

    public static String toString(VarSnippet snippet, String value) {

        return snippet.typeName() + " " + snippet.name() + " = " + value + "\n";
    }
}

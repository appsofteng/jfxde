package dev.jfxde.jx.tools;

import java.util.function.Consumer;

public abstract class Compiler {

    public static Compiler get(String language) {
        Compiler compiler = null;

        if ("java".equals(language)) {
            compiler = new JavaCompiler();
        }

        return compiler;
    }
    abstract void compile(Iterable<String> options, String code, Consumer<Diagnostic> consumer);

}

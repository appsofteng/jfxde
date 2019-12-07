package dev.jfxde.jx.tools;

import java.util.List;
import java.util.function.Consumer;

import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

class JavaCompiler extends Compiler {

    private javax.tools.JavaCompiler compiler;

    @Override
    void compile(Iterable<String> options, String code, Consumer<Diagnostic> consumer) {
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
        }

        Iterable<? extends JavaFileObject> compilationUnits = List.of(new StringJavaSource("", code));
        CompilationTask task = compiler.getTask(null, null, null, options, null, compilationUnits);
        task.call();
    }
}

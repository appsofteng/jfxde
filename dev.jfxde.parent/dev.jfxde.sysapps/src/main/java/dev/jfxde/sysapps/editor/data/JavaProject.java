package dev.jfxde.sysapps.editor.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import dev.jfxde.jx.tools.StringJavaSource;


class JavaProject extends Project {

    private static final Logger LOGGER = Logger.getLogger(JavaProject.class.getName());

    private static final String MAIN = "src/main";
    private static final String MAIN_JAVA = MAIN + "/java";
    private static final String MAIN_RESOURCES = MAIN + "/resources";
    private static final String TEST = "src/test";
    private static final String TEST_JAVA = TEST + "/java";
    private static final String TEST_RESOURCES = TEST + "/resources";
    private static final String TARGET_CLASSES = "target/classes";
    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private javax.tools.JavaCompiler compiler;

    @Override
    public void create(Path path) {
        try {
            Files.createDirectories(path.resolve(MAIN_JAVA));
            Files.createDirectories(path.resolve(MAIN_RESOURCES));
            Files.createDirectories(path.resolve(TEST_JAVA));
            Files.createDirectories(path.resolve(TEST_RESOURCES));
            Files.createDirectories(path.resolve(TARGET_CLASSES));
            Files.createDirectories(path.resolve(TARGET_TEST_CLASSES));
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    void compile(Path path, String code, Consumer<List<Diagnostic<?>>> consumer) {
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
        }

        Iterable<? extends JavaFileObject> compilationUnits = List.of(new StringJavaSource("", code));

        List<Diagnostic<?>> diags = Collections.synchronizedList(new ArrayList<>());
        CompilationTask task = compiler.getTask(null, null, d -> diags.add(d), getCompilerOptions(), null, compilationUnits);
        task.call();

        consumer.accept(diags);
    }

    private Iterable<String> getCompilerOptions() {
        return List.of();
    }
}

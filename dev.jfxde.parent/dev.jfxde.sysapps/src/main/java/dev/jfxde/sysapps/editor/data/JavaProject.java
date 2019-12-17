package dev.jfxde.sysapps.editor.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import dev.jfxde.jx.tools.StringJavaSource;

class JavaProject extends Project {

    private static final Logger LOGGER = Logger.getLogger(JavaProject.class.getName());

    private static final String SRC = "src";
    private static final String MAIN = SRC + "/main";
    private static final String MAIN_JAVA = MAIN + "/java";
    private static final String MAIN_RESOURCES = MAIN + "/resources";
    private static final String TEST = "src/test";
    private static final String TEST_JAVA = TEST + "/java";
    private static final String TEST_RESOURCES = TEST + "/resources";
    private static final String TARGET_CLASSES = "target/classes";
    private static final String TARGET_TEST_CLASSES = "target/test-classes";

    private javax.tools.JavaCompiler compiler;
    private CompletableFuture<List<Diagnostic<?>>> future;

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

    public CompletableFuture<List<Diagnostic<?>>> compile(Path path, String code) {
        
        Path projectPath = getProjectPath(path);
        
        if (projectPath == null) {
            return null;
        }

        if (future == null) {
            future = CompletableFuture.supplyAsync(() -> compile(path, code, projectPath));
        } else {
            future = this.future.thenApplyAsync(i -> compile(path, code, projectPath));
        }

        return future;
    }

    private javax.tools.JavaCompiler getCompiler() {
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
        }
        
        return compiler;
    }
    
    private List<Diagnostic<?>> compile(Path path, String code, Path projectPath) {

        Iterable<? extends JavaFileObject> compilationUnits = List.of(new StringJavaSource(path.getFileName(), code));

        List<Diagnostic<?>> diags = Collections.synchronizedList(new ArrayList<>());
        CompilationTask task = getCompiler().getTask(null, null, d -> diags.add(d), getCompilerOptions(projectPath), null, compilationUnits);
        task.call();

        return diags;
    }

    private Iterable<String> getCompilerOptions(Path projectPath) {
        List<String> options = new ArrayList<>();
             
        Path classOutputDir = getClassOutputPath(projectPath);

        if (classOutputDir != null) {
            options.add("-d");
            options.add(classOutputDir.toString());
        }
        
        options.add("-Xlint");

        return options;
    }
    
    private Path getClassOutputPath(Path projectPath) {
        Path outputDir = null;
        if (projectPath != null) {
            outputDir = projectPath.resolve(TARGET_CLASSES);
        }
        
        return outputDir;
    }
    
    private Path getProjectPath(Path path) {
        Path projectPath = null;
        Path parent = path;
        
        while (parent != null && !parent.endsWith(SRC)) {
            parent = parent.getParent();
        }
        
        if (parent != null) {
            projectPath = parent.getParent();
        }
        
        return projectPath;
    }
}

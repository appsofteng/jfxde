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
import javax.tools.FileObject;
import javax.tools.ForwardingJavaFileManager;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
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

    private static JavaCompiler compiler;
    private CompletableFuture<List<Diagnostic<Path>>> future;

    public JavaProject(String kind, Path path) {
        super(kind, path);
    }

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

    @Override
    public CompletableFuture<List<Diagnostic<Path>>> compile(Path path, String code) {

        Path projectPath = getPath();

        if (projectPath == null) {
            return null;
        }

        Iterable<? extends JavaFileObject> compilationUnits = List.of(new StringJavaSource(path, code));
        List<Diagnostic<Path>> diags = Collections.synchronizedList(new ArrayList<>());

        if (future == null) {
            future = CompletableFuture.supplyAsync(() -> compile(projectPath, compilationUnits, diags));
        } else {
            future = future.thenApplyAsync(i -> compile(projectPath, compilationUnits, diags));
        }

        return future;
    }

    private List<Diagnostic<Path>> compile(Path projectPath, Iterable<? extends JavaFileObject> compilationUnits, List<Diagnostic<Path>> diags) {

        StandardJavaFileManager stdFileManager = getCompiler()
                .getStandardFileManager(d -> diags.add(new DiagnosticWrapper<>(d, Path.of(d.getSource().toUri()))), null, null);

        JavaFileManager fileManager = new ForwardingJavaFileManager<>(stdFileManager) {
            public boolean contains(JavaFileManager.Location location, FileObject fo) throws IOException {

                if (fo instanceof StringJavaSource) {
                    return true;
                } else {

                    return super.contains(location, fo);
                }
            }
        };

        CompilationTask task = getCompiler().getTask(null, fileManager, d -> diags.add(new DiagnosticWrapper<>(d, Path.of(d.getSource().toUri()))),
                getCompilerOptions(projectPath), null, compilationUnits);
        task.call();

        return diags;
    }

    @Override
    List<Diagnostic<Path>> compile(Path projectPath, List<Path> paths, List<Diagnostic<Path>> diags) {

        StandardJavaFileManager fileManager = getCompiler()
                .getStandardFileManager(d -> diags.add(new DiagnosticWrapper<>(d, Path.of(d.getSource().toUri()))), null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromPaths(paths);

        CompilationTask task = getCompiler().getTask(null, fileManager, d -> diags.add(new DiagnosticWrapper<>(d, Path.of(d.getSource().toUri()))),
                getCompilerOptions(projectPath), null, compilationUnits);
        task.call();

        try {
            fileManager.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return diags;
    }

    private JavaCompiler getCompiler() {
        if (compiler == null) {
            compiler = ToolProvider.getSystemJavaCompiler();
        }

        return compiler;
    }

    private Iterable<String> getCompilerOptions(Path projectPath) {

        Path destinationtDir = getDestinationPath(projectPath);

        List<String> options = new JavaOptions()
                .addSourcePath(getSourcePath(projectPath))
                .setDestinationDirectory(destinationtDir)
                .add("-Xlint")
                .build();

        return options;
    }

    private Path getSourcePath(Path projectPath) {
        Path path = null;
        if (projectPath != null) {
            path = projectPath.resolve(MAIN_JAVA);
        }

        return path;
    }

    private Path getDestinationPath(Path projectPath) {
        Path outputDir = null;
        if (projectPath != null) {
            outputDir = projectPath.resolve(TARGET_CLASSES);
        }

        return outputDir;
    }

    @Override
    Path getProjectPath(Path path) {
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

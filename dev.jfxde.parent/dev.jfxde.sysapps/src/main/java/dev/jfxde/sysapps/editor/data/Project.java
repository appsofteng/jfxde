package dev.jfxde.sysapps.editor.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.tools.Diagnostic;

import dev.jfxde.j.nio.file.XFiles;

public abstract class Project {

    private static final Map<String, Project> CACHE = new HashMap<>();

    public static Project get(String kind) {
        Project project = null;
        if ("java".equals(kind)) {
            project = CACHE.computeIfAbsent(kind, k -> new JavaProject());
        } else {
            project = new EmptyProject();
        }

        return project;
    }

    public static Project get(Path path) {
        String extension = XFiles.getFileExtension(path.toString());

        return get(extension);
    }

    public void create(Path path) {
    }

    public CompletableFuture<List<Diagnostic<?>>> compile(Path path, String code) {

        return null;
    }
}

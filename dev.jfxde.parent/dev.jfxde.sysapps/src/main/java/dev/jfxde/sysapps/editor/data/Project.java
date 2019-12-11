package dev.jfxde.sysapps.editor.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.tools.Diagnostic;

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

    public void create(Path path) {
    }

    void compile(Path path, String code, Consumer<List<Diagnostic<?>>> consumer) {
    }
}

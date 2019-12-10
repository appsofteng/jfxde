package dev.jfxde.sysapps.editor.data;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

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
}

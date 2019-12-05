package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.Map;

import dev.jfxde.logic.data.FXPath;

public abstract class Project {

    private static final Map<FXPath, Project> CACHE = new HashMap<>();

    static Project get(String kind, FXPath path) {
        Project project = null;
        if ("java".equals(kind)) {
            project = CACHE.computeIfAbsent(path, p -> new JavaProject(path));
        } else {
            project = new EmptyProject();
        }

        return project;
    }

    abstract void create();
}

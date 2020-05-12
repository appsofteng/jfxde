package dev.jfxde.sysapps.editor.data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import javax.tools.Diagnostic;

import dev.jfxde.j.nio.file.XFiles;

public abstract class Project {
    
    private String kind;
    private Path path;
    
    public Project() {
    }
    
    public Project(String kind, Path path) {
        this.kind = kind;
        this.path = getProjectPath(path);
    }

    Path getProjectPath(Path path) {
        return path;
    }

    public static Project get(String kind) {
        return get(kind, null);
    }

    public static Project get(Path path) {
        String extension = XFiles.getFileExtension(path.toString());

        return get(extension, path);
    }
    
    private static Project get(String kind, Path path) {
        Project project = null;
        if ("java".equals(kind)) {
            project = new JavaProject(kind, path);
        } else {
            project = new EmptyProject();
        }

        return project;
    }

    public void create(Path path) {
    }
    
    Path getPath() {
        return path;
    }

    public static Map<Path,List<Diagnostic<Path>>> compile(List<Path> paths) {
        
        Map<Path, Project> projects = new HashMap<>();
        Map<Path, List<Path>> projectPaths = new HashMap<>();
        
        for (Path path : paths) {
            Project project = Project.get(path);
            Path projectPath = project.getPath();
            
            projects.put(projectPath, project);
            projectPaths.computeIfAbsent(projectPath, k -> new ArrayList<>()).add(path);
        }
        
        List<Diagnostic<Path>> diags = Collections.synchronizedList(new ArrayList<>());
        
        for (Path projectPath : projectPaths.keySet()) {
            
           projects.get(projectPath).compile(projectPath, projectPaths.get(projectPath), diags);
        }        

        var result = diags.stream().sorted(Comparator.comparing(Diagnostic::getKind)).collect(Collectors.groupingBy(d -> d.getSource()));
        
        return result;
    }
    
    public CompletableFuture<List<Diagnostic<Path>>> compile(Path path, String code) {
        return null;
    }    
    
    List<Diagnostic<Path>> compile(Path projectPath, List<Path> paths, List<Diagnostic<Path>> diags) {
        return null;
    }
}

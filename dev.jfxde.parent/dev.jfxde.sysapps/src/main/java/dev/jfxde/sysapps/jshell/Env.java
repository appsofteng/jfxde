package dev.jfxde.sysapps.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Env implements Comparable<Env> {

    private String name;
    private List<String> classPath = new ArrayList<>();
    private List<String> modulePath = new ArrayList<>();
    private List<String> addModules = new ArrayList<>();
    private List<String> moduleLocations = new ArrayList<>();
    private List<ExportItem> addExports = new ArrayList<>();

    public Env() {
    }

    public Env(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getClassPath() {
        return classPath;
    }

    public List<String> getModulePath() {
        return modulePath;
    }

    public List<String> getAddModules() {
        return addModules;
    }

    public List<String> getModuleLocations() {
        return moduleLocations;
    }

    public List<ExportItem> getAddExports() {
        return addExports;
    }

    public String[] getOptions() {

        List<String> options = new ArrayList<>();

        if (!classPath.isEmpty()) {
            options.add("--class-path");
            options.add(classPath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!modulePath.isEmpty()) {
            options.add("--module-path");
            options.add(modulePath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!addModules.isEmpty()) {
            options.add("--add-modules");
            options.add(addModules.stream().collect(Collectors.joining(",")));
        }

        if (!addExports.isEmpty()) {
            addExports.forEach(e -> {
                options.add("--add-exports");
                options.add(e.toString());
            });
        }

        return options.toArray(new String[] {});
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public int compareTo(Env o) {
        return name.compareTo(o.name);
    }
}

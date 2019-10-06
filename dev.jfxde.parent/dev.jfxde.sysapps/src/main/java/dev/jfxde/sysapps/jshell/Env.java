package dev.jfxde.sysapps.jshell;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Env implements Comparable<Env> {

    private String name;
    private ObservableList<String> classPath = FXCollections.observableArrayList();
    private ObservableList<String> modulePath = FXCollections.observableArrayList();
    private ObservableList<String> addModules = FXCollections.observableArrayList();
    private ObservableList<ExportItem> addExports = FXCollections.observableArrayList();

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

    public ObservableList<String> getClassPath() {
        return classPath;
    }

    public ObservableList<String> getModulePath() {
        return modulePath;
    }

    public ObservableList<String> getAddModules() {
        return addModules;
    }

    public ObservableList<ExportItem> getAddExports() {
        return addExports;
    }

    public String[] getOptions() {

        List<String> options = new ArrayList<>();

        if (classPath != null) {
            options.add("--class-path " + classPath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (modulePath != null) {
            options.add("--module-path " + modulePath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (addModules != null) {
            options.add("--add-modules " + addModules.stream().collect(Collectors.joining(",")));
        }

        if (addExports != null) {
            options.addAll(addExports.stream().map(s -> "--add-exports " + s).collect(Collectors.toList()));
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

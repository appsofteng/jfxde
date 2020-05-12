package dev.jfxde.sysapps.editor.data;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class JavaOptions {

    private List<String> sourcePath = new ArrayList<>();
    private List<String> modulePath = new ArrayList<>();
    private List<String> classPath = new ArrayList<>();
    private List<String> options = new ArrayList<>();
    private String destinationDirectory = "";

    public JavaOptions setDestinationDirectory(Path destinationDirectory) {

        this.destinationDirectory = destinationDirectory == null ? "" : destinationDirectory.toString();

        return this;
    }

    public JavaOptions addSourcePath(Path path) {
        if (path != null) {
            sourcePath.add(path.toString());
        }

        return this;
    }

    public JavaOptions add(String option) {
        options.add(option);

        return this;
    }

    public List<String> build() {
        List<String> build = new ArrayList<>(options);

        if (!sourcePath.isEmpty()) {
            build.add("-sourcepath");
            build.add(sourcePath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!modulePath.isEmpty()) {
            build.add("-p");
            build.add(modulePath.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        List<String> cp = new ArrayList<String>(classPath);

        if (!destinationDirectory.isEmpty()) {
            cp.add(destinationDirectory);
        }

        if (!cp.isEmpty()) {
            build.add("-cp");
            build.add(cp.stream().collect(Collectors.joining(File.pathSeparator)));
        }

        if (!destinationDirectory.isEmpty()) {
            build.add("-d");
            build.add(destinationDirectory);
        }

        return build;
    }
}

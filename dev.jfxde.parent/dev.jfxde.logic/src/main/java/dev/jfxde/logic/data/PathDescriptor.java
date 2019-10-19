package dev.jfxde.logic.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.property.ReadOnlyProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;

public class PathDescriptor implements Comparable<PathDescriptor> {

    private Path path;
    private StringProperty name;

    public PathDescriptor(Path path) {
        this.path = path;
        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());
    }

    public PathDescriptor(Path path, String name) {
        this.path = path;
        setName(name);
    }

    public Path getPath() {
        return path;
    }

    public void setName(String value) {
        nameProperty().set(value);
    }

    public StringProperty nameProperty() {

        if (name == null) {
            name = new SimpleStringProperty() {
                @Override
                public Object getBean() {
                    return PathDescriptor.this;
                }

                @Override
                public String toString() {
                    return get();
                }
            };
        }

        return name;
    }

    public boolean isLeaf() {
        boolean result = !Files.isDirectory(path) || !Files.isReadable(path);

        try {
            result = result || !isRoot()
                    && !Files.list(path)
                            .filter(p -> Files.isDirectory(p))
                            .filter(p -> Files.isReadable(p))
                            .findAny().isPresent();
        } catch (Exception e) {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, path.toString(), e);
        }

        return result;
    }

    boolean isRoot() {
        return path.toString().isEmpty();
    }

    public <T> List<T> getDirectories(Function<PathDescriptor, T> mapper) {
        List<T> result = List.of();

        if (!Files.isReadable(path)) {
            return result;
        }

        try {

            if (isRoot()) {
                result = Arrays.stream(File.listRoots())
                        .map(File::toPath)
                        .filter(p -> Files.isDirectory(p))
                        .filter(p -> Files.isReadable(p))
                        .map(PathDescriptor::new)
                        .sorted()
                        .map(mapper)
                        .collect(Collectors.toList());
            } else {

                result = Files.list(path)
                        .filter(p -> Files.isDirectory(p))
                        .filter(p -> Files.isReadable(p))
                        .map(PathDescriptor::new)
                        .sorted()
                        .map(mapper)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public <T> List<T> getFiles(Function<PathDescriptor, T> mapper) {
        List<T> result = List.of();

        try {
            result = Files.list(path)
                    .filter(p -> !Files.isDirectory(p))
                    .map(PathDescriptor::new)
                    .sorted()
                    .map(mapper)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public int compareTo(PathDescriptor o) {
        int result = Boolean.compare(!Files.isDirectory(path), !Files.isDirectory(o.path));

        if (result == 0) {
            result = name.get().compareToIgnoreCase(o.name.get());
        }

        return result;
    }

    public static class StringComparator implements Comparator<StringProperty> {

        @Override
        public int compare(StringProperty o1, StringProperty o2) {
            PathDescriptor desc1 = (PathDescriptor) o1.getBean();
            PathDescriptor desc2 = (PathDescriptor) o2.getBean();

            int result = Boolean.compare(!Files.isDirectory(desc1.getPath()), !Files.isDirectory(desc2.getPath()));

            if (result == 0) {
                result = o1.getValue().compareToIgnoreCase(o2.getValue());
            }

            return result;
        }
    }
}

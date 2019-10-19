package dev.jfxde.logic.data;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PathDescriptor implements Comparable<PathDescriptor> {

    private static final String TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String ROOT_NAME = File.separator;
    private Path path;
    private StringProperty name;
    private LongProperty size;
    private ObjectProperty<FileTime> created;
    private ObjectProperty<FileTime> modified;

    public PathDescriptor() {
        this.path = Paths.get(ROOT_NAME);
        setName(ROOT_NAME);
    }

    public PathDescriptor(Path path) {
        this.path = path;
        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());

        try {
            var fileAttributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
            setCreated(fileAttributes.readAttributes().creationTime());
            setModified(fileAttributes.readAttributes().lastModifiedTime());

            if (!Files.isDirectory(path)) {
                setSize(Files.size(path));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public Path getPath() {
        return path;
    }

    private void setName(String value) {
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

    private void setSize(long value) {
        getSizeProperty().set(value);
    }

    public ReadOnlyLongProperty sizeProperty() {
        return getSizeProperty();
    }

    private LongProperty getSizeProperty() {

        if (size == null) {
            size = new SimpleLongProperty() {
                @Override
                public Object getBean() {
                    return PathDescriptor.this;
                }

                @Override
                public String toString() {
                    return Files.isDirectory(path) ? "" : NumberFormat.getInstance().format(Math.ceil(get() / 1024.0)) + " KiB";
                }
            };
        }

        return size;
    }

    private void setCreated(FileTime value) {
        getCreatedProperty().set(value);
    }

    public ReadOnlyObjectProperty<FileTime> createdProperty() {
        return getCreatedProperty();
    }

    private ObjectProperty<FileTime> getCreatedProperty() {
        if (created == null) {
            created = new SimpleObjectProperty<>() {

                @Override
                public Object getBean() {
                    return PathDescriptor.this;
                }

                public String toString() {
                    return new SimpleDateFormat(TIME_FORMAT).format(get().toMillis());
                };

            };
        }

        return created;
    }

    private void setModified(FileTime value) {
        getModifiedProperty().set(value);
    }

    public ReadOnlyObjectProperty<FileTime> modifiedProperty() {
        return getModifiedProperty();
    }

    private ObjectProperty<FileTime> getModifiedProperty() {
        if (modified == null) {
            modified = new SimpleObjectProperty<>() {

                @Override
                public Object getBean() {
                    return PathDescriptor.this;
                }

                public String toString() {
                    return new SimpleDateFormat(TIME_FORMAT).format(get().toMillis());
                };

            };
        }

        return modified;
    }

    public boolean isDirectory() {
        return Files.isDirectory(path);
    }

    public boolean isReadable() {
        return Files.isReadable(path);
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
            throw new RuntimeException(e);
        }

        return result;
    }

    boolean isRoot() {
        return ROOT_NAME.equals(path.toString());
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
                        .map(PathDescriptor::new)
                        .sorted()
                        .map(mapper)
                        .collect(Collectors.toList());
            } else {

                result = Files.list(path)
                        .filter(p -> Files.isDirectory(p))
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
            if (!isRoot()) {
                result = Files.list(path)
                        .filter(p -> !Files.isDirectory(p))
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

    public static class LongComparator implements Comparator<ReadOnlyLongProperty> {

        @Override
        public int compare(ReadOnlyLongProperty o1, ReadOnlyLongProperty o2) {

            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            PathDescriptor desc1 = (PathDescriptor) o1.getBean();
            PathDescriptor desc2 = (PathDescriptor) o2.getBean();

            int result = Boolean.compare(!Files.isDirectory(desc1.getPath()), !Files.isDirectory(desc2.getPath()));

            if (result == 0) {
                result = o1.asObject().get().compareTo(o2.asObject().get());
            }

            return result;
        }
    }

    public static class ObjectComparator<T extends Comparable<T>> implements Comparator<ReadOnlyObjectProperty<T>> {

        @Override
        public int compare(ReadOnlyObjectProperty<T> o1, ReadOnlyObjectProperty<T> o2) {

            if (o1 == null && o2 == null) {
                return 0;
            } else if (o1 == null) {
                return -1;
            } else if (o2 == null) {
                return 1;
            }

            PathDescriptor desc1 = (PathDescriptor) o1.getBean();
            PathDescriptor desc2 = (PathDescriptor) o2.getBean();

            int result = 0;

            if (desc1.isDirectory() && !desc2.isDirectory()) {
                result = -1;
            } else if (!desc1.isDirectory() && desc2.isDirectory()) {
                result = -1;
            }

            if (result == 0) {
                result = o1.get().compareTo(o2.get());
            }

            return result;
        }
    }
}

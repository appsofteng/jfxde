package dev.jfxde.logic.data;

import java.io.File;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributeView;
import java.nio.file.attribute.FileTime;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dev.jfxde.jfxext.nio.file.FileUtils;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PathDescriptor implements Comparable<PathDescriptor> {

    private static final String TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final String ROOT_NAME = File.separator;
    private static final PathDescriptor ROOT = createRoot();
    private static final PathDescriptor EMPTY = createEmpty();

    private Path path;
    private StringProperty name;
    private LongProperty size;
    private ObjectProperty<FileTime> created;
    private ObjectProperty<FileTime> modified;
    private boolean directory;
    private PathDescriptor parent;
    private final static Map<Path, WeakReference<PathDescriptor>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private ObservableList<PathDescriptor> directories = FXCollections.observableArrayList();
    private ObservableList<PathDescriptor> files = FXCollections.observableArrayList();
    private ObservableList<PathDescriptor> paths = FXCollections.observableArrayList();
    private boolean loaded;
    private Boolean dirLeaf;
    private Boolean leaf;
    private BooleanProperty deleted = new SimpleBooleanProperty();

    public PathDescriptor() {
    }

    public PathDescriptor(String path) {
        this(null, Paths.get(path));
    }

    public PathDescriptor(PathDescriptor parent, Path path) {
        this(parent, path, Files.isDirectory(path));
    }

    public PathDescriptor(PathDescriptor parent, Path path, boolean dir) {
        this.parent = parent;
        this.path = path;
        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());
        this.directory = dir;

        try {
            var fileAttributes = Files.getFileAttributeView(path, BasicFileAttributeView.class);
            setCreated(fileAttributes.readAttributes().creationTime());
            setModified(fileAttributes.readAttributes().lastModifiedTime());

            if (!dir) {
                setSize(Files.size(path));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PathDescriptor createRoot() {
        var root = new PathDescriptor();
        root.path = Paths.get(ROOT_NAME);
        root.setName(ROOT_NAME);
        root.directory = true;

        return root;
    }

    private static PathDescriptor createEmpty() {

        var empty = new PathDescriptor();
        empty.path = null;
        empty.leaf = true;
        empty.dirLeaf = true;
        empty.loaded = true;

        return empty;
    }

    public static PathDescriptor getRoot() {
        return ROOT;
    }

    public static PathDescriptor getEmpty() {
        return EMPTY;
    }

    public static PathDescriptor get(String path) {
        var key = Paths.get(path);
        var pd = CACHE.computeIfAbsent(key, k -> new WeakReference<>(new PathDescriptor(path)));

        return pd.get();
    }

    public ReadOnlyBooleanProperty deletedProperty() {
        return deleted;
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
                    return directory ? "" : NumberFormat.getInstance().format(Math.ceil(get() / 1024.0)) + " KiB";
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
        return directory;
    }

    public boolean isFile() {
        return !directory;
    }

    public boolean isReadable() {
        return Files.isReadable(path);
    }

    public boolean isDirLeaf() {

        if (dirLeaf == null) {
            dirLeaf = !directory || !Files.isReadable(path) || !isRoot() && !FileUtils.hasSubDirs(path);
        }

        return dirLeaf;
    }

    public boolean isLeaf() {

        if (leaf == null) {
            leaf = !directory || !Files.isReadable(path) || !isRoot() && !FileUtils.isEmpty(path);
        }

        return leaf;
    }

    boolean isRoot() {
        return ROOT_NAME.equals(path.toString());
    }

    public ObservableList<PathDescriptor> getDirectories() {
        return directories;
    }

    public ObservableList<PathDescriptor> getFiles() {
        return files;
    }

    public ObservableList<PathDescriptor> getPaths() {
        return paths;
    }

    public void load() {

        if (loaded) {
            return;
        }

        if (!Files.isReadable(path)) {
            return;
        }

        loaded = true;

        ForkJoinPool.commonPool().execute(() -> {
            if (isRoot()) {
                listRoots();
            } else {

                list();
            }
        });
    }

    private void listRoots() {
        try (var stream = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)) {
            var roots = stream.map(p -> new PathDescriptor(this, p, true)).collect(Collectors.toList());
            directories.setAll(roots);
            paths.setAll(roots);
            roots.forEach(p -> CACHE.put(p.path, new WeakReference<>(p)));
        }
    }

    private void list() {
        try (var stream = Files.newDirectoryStream(path)) {
            var iterator = stream.iterator();

            while (iterator.hasNext()) {
                var p = iterator.next();
                boolean directory = Files.isDirectory(p);
                var pd = new PathDescriptor(this, p, directory);
                CACHE.put(path, new WeakReference<>(pd));
                paths.add(pd);
                if (directory) {
                    directories.add(pd);
                } else {
                    files.add(pd);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public <T> void getDirectories(Function<PathDescriptor, T> mapper, Consumer<T> consumer) {

        if (!Files.isReadable(path)) {
            return;
        }
        if (isRoot()) {
            listRoots(mapper, consumer);
        } else {

            list(p -> consumer.accept(mapper.apply(new PathDescriptor(this, p, true))), p -> {
            }, consumer);
        }
    }

    public <T> void getFiles(Function<PathDescriptor, T> mapper, Consumer<T> consumer) {
        if (!isRoot()) {

            list(p -> {
            }, p -> consumer.accept(mapper.apply(new PathDescriptor(this, p, false))), consumer);
        }
    }

    public <T> void getAll(Function<PathDescriptor, T> mapper, Consumer<T> consumer) {

        if (!Files.isReadable(path)) {
            return;
        }
        if (isRoot()) {
            listRoots(mapper, consumer);
        } else {

            var files = new ArrayList<T>();
            list(p -> consumer.accept(mapper.apply(new PathDescriptor(this, p, true))),
                    p -> files.add(mapper.apply(new PathDescriptor(this, p, false))), p -> {
                    });
            files.forEach(f -> consumer.accept(f));
            consumer.accept(null);
        }
    }

    private <T> void listRoots(Function<PathDescriptor, T> mapper, Consumer<T> consumer) {
        try (var stream = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)) {
            stream.map(p -> new PathDescriptor(this, p, true))
                    // .sorted()
                    .map(mapper)
                    .forEach(consumer);
            consumer.accept(null);
        }
    }

    private <T> void list(Consumer<Path> dirs, Consumer<Path> files, Consumer<T> end) {
        try (var stream = Files.newDirectoryStream(path)) {
            var iterator = stream.iterator();

            while (iterator.hasNext()) {
                var p = iterator.next();
                boolean directory = Files.isDirectory(p);
                if (directory) {
                    dirs.accept(p);
                } else {
                    files.accept(p);
                }
            }

            end.accept(null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PathDescriptor)) {
            return false;
        }

        return path.equals(((PathDescriptor) obj).path);
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }

    @Override
    public int compareTo(PathDescriptor o) {
        int result = Boolean.compare(!directory, !o.directory);

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

            int result = Boolean.compare(!desc1.directory, !desc2.directory);

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

            int result = Boolean.compare(!desc1.directory, !desc2.directory);

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

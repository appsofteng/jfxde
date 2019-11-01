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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dev.jfxde.jfxext.nio.file.FileUtils;
import javafx.beans.property.LongProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PathDescriptor implements Comparable<PathDescriptor> {

    private static final String TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final Path ROOT_PATH = Paths.get(File.separator);
    private final static Map<Path, WeakReference<PathDescriptor>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private Path path;
    private StringProperty name;
    private String newName;
    private LongProperty size;
    private ObjectProperty<FileTime> created;
    private ObjectProperty<FileTime> modified;
    private boolean directory;
    private Set<PathDescriptor> parents = new HashSet<>();
    private ObservableList<PathDescriptor> paths = FXCollections.observableArrayList();
    private boolean loaded;
    private Boolean dirLeaf;
    private Boolean leaf;

    private PathDescriptor() {
    }

    private PathDescriptor(PathDescriptor parent, Path path, boolean dir) {
        if (parent != null) {
            this.parents.add(parent);
        }
        this.path = path;
        this.directory = dir;

        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());

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

    public static PathDescriptor getRoot() {
        // Create a new root path so that it is not strongly referenced and thus removed
        // from the cache.
        return getFromCache(null, Paths.get(ROOT_PATH.toString()), true);
    }

    public static PathDescriptor getNoname(List<String> paths) {

        var noname = new PathDescriptor();
        List<PathDescriptor> pds = paths.stream()
                .map(p -> Paths.get(p))
                .map(p -> getFromCache(noname, p, Files.isDirectory(p)))
                .collect(Collectors.toList());

        noname.setName("");
        noname.path = null;
        noname.directory = !pds.isEmpty();
        noname.leaf = !noname.directory;
        noname.dirLeaf = !noname.directory;
        noname.loaded = true;
        noname.paths.setAll(pds);

        return noname;
    }

    static PathDescriptor createDirectory(PathDescriptor parent, Path path) {
        var pathDescriptor = add(parent, path, true);
        parent.dirLeaf = false;
        parent.leaf = false;
        parent.loaded = true;
        return pathDescriptor;
    }

    static PathDescriptor createFile(PathDescriptor parent, Path path) {
        var pathDescriptor = add(parent, path, false);
        parent.leaf = false;
        parent.loaded = true;
        return pathDescriptor;
    }

    void move(PathDescriptor newParent, Path newPath) {
        CACHE.remove(path);
        parents.forEach(p -> p.remove(this));
        path = newPath;
        setName(path.getFileName().toString());
        CACHE.put(path, new WeakReference<>(this));
        add(newParent, this);
    }

    PathDescriptor copy(PathDescriptor newParent, Path newPath) {
        var pathDescriptor = add(newParent, newPath, Files.isDirectory(newPath));
        newParent.dirLeaf = false;
        newParent.leaf = false;
        newParent.loaded = true;
        return pathDescriptor;
    }

    void delete() {
        CACHE.remove(path);
        parents.forEach(p -> p.remove(this));
    }

    private void remove(PathDescriptor pd) {
        paths.remove(pd);

        if (paths.isEmpty()) {
            dirLeaf = true;
            leaf = true;
        }
    }

    public Path getPath() {
        return path;
    }

    public String getName() {
        return name.get();
    }

    private void setName(String value) {
        nameProperty().set(value);
    }

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
    }

    void rename(Path target, String name) {
        var old = path;
        path = target;
        CACHE.put(path, new WeakReference<>(this));

        if (isDirectory()) {
            paths.forEach(p -> p.rename(old, path));
        }

        setName(name);
    }

    private void rename(Path oldParent, Path newParent) {
        var relative = oldParent.relativize(path);
        path = newParent.resolve(relative);
        CACHE.put(path, new WeakReference<>(this));
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
        return ROOT_PATH.equals(path);
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
            stream.forEach(p -> {
                add(this, p, true);
            });
        }
    }

    private void list() {
        try (var stream = Files.newDirectoryStream(path)) {
            var iterator = stream.iterator();

            while (iterator.hasNext()) {
                var p = iterator.next();
                boolean directory = Files.isDirectory(p);
                add(this, p, directory);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static PathDescriptor add(PathDescriptor parent, Path p, boolean directory) {
        var pd = getFromCache(parent, p, directory);
        parent.paths.add(pd);

        return pd;
    }

    private static void add(PathDescriptor parent, PathDescriptor pd) {
        parent.paths.add(pd);
    }

    private static PathDescriptor getFromCache(PathDescriptor parent, Path path, boolean dir) {

        var pd = CACHE.computeIfAbsent(path, k -> new WeakReference<>(new PathDescriptor(parent, path, dir))).get();

        if (parent != null) {
            pd.parents.add(parent);
        }

        return pd;
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

        return path != null ? path.equals(((PathDescriptor) obj).path) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : super.hashCode();
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

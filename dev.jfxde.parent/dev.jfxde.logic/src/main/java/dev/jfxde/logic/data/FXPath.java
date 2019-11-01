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

import dev.jfxde.j.nio.file.XFiles;
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

public class FXPath implements Comparable<FXPath> {

    private static final String TIME_FORMAT = "dd-MM-yyyy HH:mm:ss";
    private static final Path ROOT_PATH = Paths.get(File.separator);
    private final static Map<Path, WeakReference<FXPath>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    private Path path;
    private StringProperty name;
    private String newName;
    private LongProperty size;
    private ObjectProperty<FileTime> created;
    private ObjectProperty<FileTime> modified;
    private BooleanProperty directory = new SimpleBooleanProperty();
    private Set<FXPath> parents = new HashSet<>();
    private ObservableList<FXPath> paths = FXCollections.observableArrayList();
    private boolean loaded;
    private Boolean dirLeaf;
    private Boolean leaf;

    private FXPath() {
    }

    private FXPath(FXPath parent, Path path, boolean dir) {
        if (parent != null) {
            this.parents.add(parent);
        }
        this.path = path;
        setDirectory(dir);

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

    public static FXPath getRoot() {
        // Create a new root path so that it is not strongly referenced and thus removed
        // from the cache.
        return getFromCache(null, Paths.get(ROOT_PATH.toString()), true);
    }

    public static FXPath getPseudoRoot(List<String> paths) {

        var pseudoRoot = new FXPath();
        List<FXPath> pds = paths.stream()
                .map(p -> Paths.get(p))
                .map(p -> getFromCache(pseudoRoot, p, Files.isDirectory(p)))
                .collect(Collectors.toList());

        pseudoRoot.setName("");
        pseudoRoot.path = null;
        pseudoRoot.setDirectory(!pds.isEmpty());
        pseudoRoot.leaf = !pseudoRoot.isDirectory();
        pseudoRoot.dirLeaf = !pseudoRoot.isDirectory();
        pseudoRoot.loaded = true;
        pseudoRoot.paths.setAll(pds);

        return pseudoRoot;
    }

    static FXPath createDirectory(FXPath parent, Path path) {
        var pathDescriptor = add(parent, path, true);
        parent.dirLeaf = false;
        parent.leaf = false;
        parent.loaded = true;
        return pathDescriptor;
    }

    static FXPath createFile(FXPath parent, Path path) {
        var pathDescriptor = add(parent, path, false);
        parent.leaf = false;
        parent.loaded = true;
        return pathDescriptor;
    }

    void rename(Path newPath, String newName) {
        CACHE.remove(path);
        var oldPath = path;
        path = newPath;
        CACHE.put(path, new WeakReference<>(this));

        paths.forEach(p -> p.rename(oldPath, path));

        setName(newName);
    }

    private void rename(Path oldParent, Path newParent) {
        CACHE.remove(path);
        var relative = oldParent.relativize(path);
        path = newParent.resolve(relative);
        CACHE.put(path, new WeakReference<>(this));

        paths.forEach(p -> p.rename(oldParent, newParent));
    }

    void move(FXPath newParent, Path newPath) {
        CACHE.remove(path);
        parents.stream().filter(p -> !p.isPseudoRoot()).forEach(p -> p.remove(this));
        var oldPath = path;
        path = newPath;
        CACHE.put(path, new WeakReference<>(this));

        paths.forEach(p -> p.rename(oldPath, path));

        newParent.dirLeaf = !isDirectory();
        newParent.leaf = false;
        newParent.loaded = true;

        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());
        newParent.paths.add(this);
    }

    static FXPath copy(FXPath newParent, Path newPath) {
        var fxpath = add(newParent, newPath, Files.isDirectory(newPath));
        newParent.dirLeaf = !fxpath.isDirectory();
        newParent.leaf = false;
        newParent.loaded = true;
        return fxpath;
    }

    void delete() {
        CACHE.remove(path);
        parents.forEach(p -> p.remove(this));
    }

    private void remove(FXPath pd) {
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

    public StringProperty nameProperty() {

        if (name == null) {
            name = new SimpleStringProperty() {
                @Override
                public Object getBean() {
                    return FXPath.this;
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
                    return FXPath.this;
                }

                @Override
                public String toString() {
                    return isDirectory() ? "" : NumberFormat.getInstance().format(Math.ceil(get() / 1024.0)) + " KiB";
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
                    return FXPath.this;
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
                    return FXPath.this;
                }

                public String toString() {
                    return new SimpleDateFormat(TIME_FORMAT).format(get().toMillis());
                };

            };
        }

        return modified;
    }

    public boolean isDirectory() {
        return directory.get();
    }

    private void setDirectory(boolean value) {
        directory.set(value);
    }

    public ReadOnlyBooleanProperty directoryProperty() {
        return directory;
    }

    public boolean isFile() {
        return !isDirectory();
    }

    public boolean isReadable() {
        return Files.isReadable(path);
    }

    public boolean isDirLeaf() {

        if (dirLeaf == null) {
            dirLeaf = !isDirectory() || !Files.isReadable(path) || !isRoot() && !XFiles.hasSubDirs(path);
        }

        return dirLeaf;
    }

    public boolean isLeaf() {

        if (leaf == null) {
            leaf = !isDirectory() || !Files.isReadable(path) || !isRoot() && !XFiles.isEmpty(path);
        }

        return leaf;
    }

    boolean isRoot() {
        return ROOT_PATH.equals(path);
    }

    boolean isPseudoRoot() {
        return path == null;
    }

    public ObservableList<FXPath> getPaths() {
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

    private static FXPath add(FXPath parent, Path p, boolean directory) {
        var pd = getFromCache(parent, p, directory);
        parent.paths.add(pd);

        return pd;
    }

    private static FXPath getFromCache(FXPath parent, Path path, boolean dir) {

        var pd = CACHE.computeIfAbsent(path, k -> new WeakReference<>(new FXPath(parent, path, dir))).get();

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
        if (!(obj instanceof FXPath)) {
            return false;
        }

        return path != null ? path.equals(((FXPath) obj).path) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return path != null ? path.hashCode() : super.hashCode();
    }

    @Override
    public int compareTo(FXPath o) {
        int result = Boolean.compare(!isDirectory(), !o.isDirectory());

        if (result == 0) {
            result = name.get().compareToIgnoreCase(o.name.get());
        }

        return result;
    }

    public static class StringComparator implements Comparator<StringProperty> {

        @Override
        public int compare(StringProperty o1, StringProperty o2) {
            FXPath desc1 = (FXPath) o1.getBean();
            FXPath desc2 = (FXPath) o2.getBean();

            int result = Boolean.compare(!desc1.isDirectory(), !desc2.isDirectory());

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

            FXPath desc1 = (FXPath) o1.getBean();
            FXPath desc2 = (FXPath) o2.getBean();

            int result = Boolean.compare(!desc1.isDirectory(), !desc2.isDirectory());

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

            FXPath desc1 = (FXPath) o1.getBean();
            FXPath desc2 = (FXPath) o2.getBean();

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

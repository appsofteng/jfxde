package dev.jfxde.logic.data;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dev.jfxde.j.nio.file.WatchServiceRegister;
import dev.jfxde.j.nio.file.XFiles;
import javafx.beans.Observable;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyLongProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class FXPath implements Comparable<FXPath> {

    private static final Path ROOT_PATH = Paths.get(File.separator);
    private final static Map<Path, WeakReference<FXPath>> CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static WatchServiceRegister watchServiceRegister;

    private Consumer<List<WatchEvent<?>>> watcher = this::watch;
    private ObjectProperty<Path> path = new SimpleObjectProperty<Path>();
    private StringProperty name;
    private String newName;
    private BooleanProperty directory = new SimpleBooleanProperty();
    private Set<FXPath> parents = new HashSet<>();
    private ObservableList<FXPath> paths = FXCollections
            .observableArrayList((p) -> new Observable[] { p.basicFileAttributes.lastModifiedTimeProperty() });
    private boolean loaded;
    private Boolean dirLeaf;
    private Boolean leaf;
    private FXBasicFileAttributes basicFileAttributes;

    private FXPath() {
    }

    private FXPath(FXPath parent, Path path, boolean dir) {
        if (parent != null) {
            this.parents.add(parent);
        }
        setPath(path);
        setDirectory(dir);

        Path fileName = path.getFileName();
        setName(fileName == null ? path.toString() : fileName.toString());

        basicFileAttributes = new FXBasicFileAttributes(this);
    }

    public static void setWatchServiceRegister(WatchServiceRegister watchServiceRegister) {
        FXPath.watchServiceRegister = watchServiceRegister;
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
        removeFromCache(getPath());
        var oldPath = getPath();
        setPath(newPath);
        getFromCache(getPath(), p -> new WeakReference<>(this), isDirectory());

        paths.forEach(p -> p.rename(oldPath, getPath()));

        setName(newName);
    }

    private void rename(Path oldParent, Path newParent) {
        removeFromCache(getPath());
        var relative = oldParent.relativize(getPath());
        setPath(newParent.resolve(relative));
        getFromCache(getPath(), p -> new WeakReference<>(this), isDirectory());

        paths.forEach(p -> p.rename(oldParent, newParent));
    }

    void move(FXPath newParent, Path newPath) {
        removeFromCache(getPath());
        new ArrayList<>(parents).stream().filter(p -> !p.isPseudoRoot()).forEach(p -> p.remove(this));
        var oldPath = getPath();
        setPath(newPath);
        getFromCache(getPath(), p -> new WeakReference<>(this), isDirectory());

        paths.forEach(p -> p.rename(oldPath, getPath()));

        newParent.dirLeaf = !isDirectory();
        newParent.leaf = false;
        newParent.loaded = true;

        Path fileName = getPath().getFileName();
        setName(fileName == null ? getPath().toString() : fileName.toString());
        parents.add(newParent);
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
        removeFromCache(getPath());
        new ArrayList<>(parents).forEach(p -> p.remove(this));
    }

    public void add(FXPath pd) {
        paths.add(pd);
        pd.parents.add(this);

        dirLeaf = !pd.isDirectory();
        leaf = false;
        loaded = true;
    }

    public void remove(FXPath pd) {
        paths.remove(pd);
        pd.parents.remove(this);

        if (paths.isEmpty()) {
            dirLeaf = true;
            leaf = true;
        }
    }

    public Path getPath() {
        return path.get();
    }

    private void setPath(Path value) {
        path.set(value);
    }

    public ReadOnlyObjectProperty<Path> pathProperty() {
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
        return Files.isReadable(getPath());
    }

    public boolean isDirLeaf() {

        if (dirLeaf == null) {
            dirLeaf = !isDirectory() || !Files.isReadable(getPath()) || !isRoot() && !XFiles.hasSubDirs(getPath());
        }

        return dirLeaf;
    }

    public boolean isLeaf() {

        if (leaf == null) {
            leaf = !isDirectory() || !Files.isReadable(getPath()) || !isRoot() && !XFiles.isEmpty(getPath());
        }

        return leaf;
    }

    boolean isRoot() {
        return ROOT_PATH.equals(getPath());
    }

    boolean isPseudoRoot() {
        return getPath() == null;
    }

    public ObservableList<FXPath> getPaths() {
        return paths;
    }

    public void refresh() {
        paths.clear();
        loaded = false;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void load() {

        if (loaded) {
            return;
        }

        if (!isReadable()) {
            return;
        }

        loaded = true;

        ForkJoinPool.commonPool().execute(() -> {
            if (isRoot()) {
                listRoots();
            } else {
                list();
            }

            if (watchServiceRegister != null) {
                watchServiceRegister.register(getPath(), watcher);
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
        try (var stream = Files.newDirectoryStream(getPath())) {
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

        var pd = getFromCache(path, k -> new WeakReference<>(new FXPath(parent, path, dir)), dir);

        if (parent != null) {
            pd.parents.add(parent);
        }

        return pd;
    }

    private static FXPath getFromCache(Path path, Function<Path, WeakReference<FXPath>> function, boolean dir) {

        var fxpath = CACHE.computeIfAbsent(path, function).get();

        return fxpath;
    }

    private static void removeFromCache(Path path) {
        CACHE.remove(path);
    }

    private void watch(List<WatchEvent<?>> events) {
        events.forEach(e -> {
            if (e.context() instanceof Path) {
                var contextPath = getPath().resolve((Path) e.context());

                if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                    add(this, contextPath, Files.isDirectory(contextPath));
                } else if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                    var fxpath = CACHE.get(contextPath).get();

                    if (fxpath != null) {
                        try {
                            fxpath.basicFileAttributes.setLastModifiedTime(Files.getLastModifiedTime(contextPath).toMillis());
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    }
                } else if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                    var fxpath = CACHE.get(contextPath).get();

                    if (fxpath != null) {
                        fxpath.delete();
                    }
                }
            }
        });
    }

    public FXBasicFileAttributes getBasicFileAttributes() {
        return basicFileAttributes;
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

        return getPath() != null ? getPath().equals(((FXPath) obj).getPath()) : super.equals(obj);
    }

    @Override
    public int hashCode() {
        return getPath() != null ? getPath().hashCode() : super.hashCode();
    }

    @Override
    public int compareTo(FXPath o) {
        int result = Boolean.compare(!isDirectory(), !o.isDirectory());

        if (result == 0) {
            result = name.get().compareToIgnoreCase(o.name.get());
        }

        return result;
    }

    public static final StringComparator STRING_COMPARATOR = new StringComparator();

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

    public static final LongComparator LONG_COMPARATOR = new LongComparator();

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
}

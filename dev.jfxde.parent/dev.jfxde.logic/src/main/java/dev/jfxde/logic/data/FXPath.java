package dev.jfxde.logic.data;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import dev.jfxde.j.nio.file.WatchServiceRegister;
import dev.jfxde.j.nio.file.XFiles;
import dev.jfxde.j.util.search.Searcher;
import dev.jfxde.jfx.embed.swing.FXUtils;
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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class FXPath implements Comparable<FXPath> {

    private static final Logger LOGGER = Logger.getLogger(FXPath.class.getName());

    private static WeakReference<FXPath> ROOT = new WeakReference<FXPath>(null);

    private final static Map<Path, WeakReference<FXPath>> CACHE = new WeakHashMap<>();
    private static WatchServiceRegister watchServiceRegister;
    private static final ReentrantLock LOCK = new ReentrantLock();

    private Consumer<List<WatchEvent<?>>> directoryWatcher;
    private List<Predicate<FXPath>> onDelete = new ArrayList<>();
    private List<Consumer<FXPath>> onDeleted = new ArrayList<>();
    private List<Consumer<FXPath>> onDeletedExternally = new ArrayList<>();
    private static List<WeakReference<Consumer<FXPath>>> onDeletedGlobally = new ArrayList<>();
    private List<Consumer<FXPath>> onModified = new ArrayList<>();

    private ObjectProperty<Path> path = new SimpleObjectProperty<Path>();
    private ObjectProperty<Image> image;
    private StringProperty name;
    private String newName;
    private BooleanProperty directory = new SimpleBooleanProperty();
    private Set<FXPath> parents = new HashSet<>();
    private ObservableList<FXPath> paths = FXCollections.observableArrayList();
    private BooleanProperty loaded = new SimpleBooleanProperty();
    private AtomicBoolean dirLeaf;
    private AtomicBoolean leaf;
    private FXBasicFileAttributes basicFileAttributes;

    private FXPath() {
        setListeners();
    }

    private FXPath(FXPath parent, Path path, boolean dir) {
        if (parent != null) {
            this.parents.add(parent);
        }

        setListeners();

        setPath(path);
        setDirectory(dir);
        if (dir) {
            directoryWatcher = this::watchDirectory;
        }

        setFileAttributes();
    }

    private void setListeners() {
        path.addListener((v, o, n) -> {
            if (n != null) {
                Path fileName = n.getFileName();
                setName(fileName == null ? n.toString() : fileName.toString());
                setImage(FXUtils.getIcon(getPath()));
            }
        });
    }

    public static void setWatchServiceRegister(WatchServiceRegister watchServiceRegister) {
        FXPath.watchServiceRegister = watchServiceRegister;
    }

    static Lock getLock() {
        return LOCK;
    }

    public List<Consumer<FXPath>> getOnModified() {
        return onModified;
    }

    public List<Predicate<FXPath>> getOnDelete() {
        return onDelete;
    }

    public List<Consumer<FXPath>> getOnDeleted() {
        return onDeleted;
    }

    public List<Consumer<FXPath>> getOnDeletedExternally() {
        return onDeletedExternally;
    }

    public static void addOnDeletedGlobally(Consumer<FXPath> consumer) {
        onDeletedGlobally.add(new WeakReference<>(consumer));
    }

    private void onDeletedGlobally(FXPath path) {
        var i = onDeletedGlobally.iterator();

        while (i.hasNext()) {
            var consumer = i.next().get();

            if (consumer == null) {
                i.remove();
            } else {
                consumer.accept(path);
            }
        }
    }

    public static FXPath getRoot() {

        FXPath root = ROOT.get();

        if (root == null) {

            root = new FXPath();
            ROOT = new WeakReference<>(root);
            root.setName("");
            root.setDirectory(true);
            root.setDirLeaf(false);
            root.setLeaf(false);
        }

        return root;
    }

    public static FXPath getPseudoPath(FXPath... paths) {
        var pseudoPath = new FXPath();
        pseudoPath.setName("");
        pseudoPath.setDirectory(paths.length > 0);
        pseudoPath.setLeaf(!pseudoPath.isDirectory());
        pseudoPath.setDirLeaf(!pseudoPath.isDirectory());
        pseudoPath.setLoaded(true);
        pseudoPath.paths.setAll(paths);

        return pseudoPath;
    }

    public static FXPath getPseudoPath(List<String> paths) {

        var pseudoPath = new FXPath();
        List<FXPath> pds = paths.stream()
                .map(p -> Path.of(p))
                .map(p -> getFromCache(pseudoPath, p, Files.isDirectory(p)))
                .collect(Collectors.toList());

        pseudoPath.setName("");
        pseudoPath.setDirectory(true);
        pseudoPath.setLeaf(pds.isEmpty());
        pseudoPath.setDirLeaf(pds.isEmpty());
        pseudoPath.setLoaded(true);
        pseudoPath.paths.setAll(pds);

        return pseudoPath;
    }

    static FXPath createDirectory(FXPath parent, Path path) {
        var pathDescriptor = addInParent(parent, path, true);
        return pathDescriptor;
    }

    static FXPath createFile(FXPath parent, Path path) {
        var pathDescriptor = addInParent(parent, path, false);
        return pathDescriptor;
    }

    void unwatchPaths() {
        paths.stream().filter(FXPath::isDirectory).forEach(p -> {
            if (watchServiceRegister != null) {
                watchServiceRegister.unwatch(p.getPath());
                p.unwatchPaths();
            }
        });
    }

    void rename(Path newPath, String newName) {
        removeFromCache(getPath());
        var oldPath = getPath();
        setPath(newPath);
        getFromCache(getPath(), p -> new WeakReference<>(this));

        paths.forEach(p -> p.rename(oldPath, getPath()));

        setName(newName);
    }

    private void rename(Path oldParent, Path newParent) {
        removeFromCache(getPath());
        var relative = oldParent.relativize(getPath());
        setPath(newParent.resolve(relative));
        getFromCache(getPath(), p -> new WeakReference<>(this));
        watch();

        paths.forEach(p -> p.rename(oldParent, newParent));
    }

    void move(FXPath newParent, Path newPath) {
        removeFromCache(getPath());
        new ArrayList<>(parents).stream().filter(p -> !p.isPseudoPath()).forEach(p -> p.remove(this));
        var oldPath = getPath();
        setPath(newPath);
        getFromCache(getPath(), p -> new WeakReference<>(this));

        paths.forEach(p -> p.rename(oldPath, getPath()));

        newParent.setDirLeaf(!isDirectory());
        newParent.setLeaf(false);
        newParent.setLoaded(true);

        parents.add(newParent);
        newParent.paths.add(this);
    }

    static FXPath copy(FXPath newParent, Path newPath) {
        var fxpath = addInParent(newParent, newPath, Files.isDirectory(newPath));

        return fxpath;
    }

    public List<FXPath> getNotToBeDeleted() {
        List<FXPath> result = new ArrayList<>();
        if (onDelete.stream().anyMatch(f -> !f.test(this))) {
            result.add(this);
        }

        paths.forEach(p -> result.addAll(p.getNotToBeDeleted()));

        return result;
    }

    void delete() {
        onDeleted.forEach(c -> c.accept(this));
        onDeletedGlobally(this);
        removeFromCache(getPath());

        delete(p -> p.delete());
    }

    private void deleteExternally() {
        onDeletedExternally.forEach(c -> c.accept(this));
        onDeletedGlobally(this);
        // Keep in cache if listened to, e.g. file in an editor can be saved again.
        if (onDeletedExternally.isEmpty()) {
            removeFromCache(getPath());
        }

        delete(p -> p.deleteExternally());
    }

    private void delete(Consumer<FXPath> delete) {
        // Only first path in tree will have parents.
        // Next paths' parents will be cleared before.
        new ArrayList<>(parents).forEach(p -> p.remove(this));
        setLoaded(false);
        setDirLeaf(true);
        setLeaf(true);
        Iterator<FXPath> i = paths.iterator();

        while (i.hasNext()) {
            var p = i.next();
            p.parents.clear();
            delete.accept(p);
            i.remove();
        }
    }

    public void add(FXPath pd) {
        paths.add(pd);
        pd.parents.add(this);

        setDirLeaf(!pd.isDirectory());
        setLeaf(false);
        setLoaded(true);
    }

    public void remove(FXPath pd) {
        paths.remove(pd);
        pd.parents.remove(this);

        if (paths.isEmpty()) {
            setDirLeaf(true);
            setLeaf(true);
            setLoaded(false);
        }
    }

    public FXPath getParent() {
        return parents.stream().filter(p -> !p.isPseudoPath()).findFirst().orElse(null);
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

    private void setImage(Image value) {
        imageProperty().set(value);
    }

    private ObjectProperty<Image> imageProperty() {
        if (image == null) {
            image = new SimpleObjectProperty<>();
        }

        return image;
    }

    public Node getGraphic() {

        ImageView imageView = new ImageView();
        imageView.imageProperty().bind(imageProperty());
        Label label = new Label("", imageView);

        return label;
    }

    public String getName() {
        return name == null ? "" : name.get();
    }

    private void setName(String value) {
        nameProperty().set(value);
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

    public String getNewName() {
        return newName;
    }

    public void setNewName(String newName) {
        this.newName = newName;
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
        return getPath() != null && Files.isReadable(getPath());
    }

    public boolean isWritable() {
        return getPath() != null && Files.isWritable(getPath());
    }

    public boolean isExecutable() {
        return getPath() != null && Files.isExecutable(getPath());
    }

    public boolean isReadOnly() {
        boolean result = getPath() == null;

        try {
            if (getPath() != null) {
                result = (boolean) Files.getAttribute(getPath(), "dos:readonly");
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }

        return result;
    }

    public boolean isDirLeaf() {

        if (dirLeaf == null) {
            setDirLeaf(!isDirectory() || !Files.isReadable(getPath()) || !XFiles.hasSubDirs(getPath()));
        }

        return dirLeaf.get();
    }

    private void setDirLeaf(boolean value) {
        if (dirLeaf == null) {
            dirLeaf = new AtomicBoolean();
        }

        dirLeaf.set(value);
    }

    public boolean isLeaf() {

        if (leaf == null) {
            setLeaf(!isDirectory() || !Files.isReadable(getPath()) || XFiles.isEmpty(getPath()));
        }

        return leaf.get();
    }

    private void setLeaf(boolean value) {
        if (leaf == null) {
            leaf = new AtomicBoolean();
        }

        leaf.set(value);
    }

    private boolean isRoot() {
        return this == ROOT.get();
    }

    public boolean isPseudoPath() {
        return getPath() == null && !isRoot();
    }

    public ObservableList<FXPath> getPaths() {
        return paths;
    }

    public void refresh() {
        paths.clear();
        setLoaded(false);
    }

    public boolean isLoaded() {
        return loadedProperty().get();
    }

    private void setLoaded(boolean value) {

        if (!isLoaded() && value) {
            loadedProperty().set(value);
            watch();
        } else {
            loadedProperty().set(value);
        }
    }

    public BooleanProperty loadedProperty() {
        return loaded;
    }

    private void watch() {
        if (watchServiceRegister != null && getPath() != null && isDirectory() && isLoaded()) {
            setPath(watchServiceRegister.register(getPath(), directoryWatcher));
        }
    }

    public void load() {

        if (isLeaf() || isLoaded()) {
            return;
        }

        ForkJoinPool.commonPool().execute(() -> {
            getLock().lock();
            try {
                if (!isLoaded()) {
                    loadSync(paths);
                }
            } finally {
                getLock().unlock();
            }
        });
    }

    private List<FXPath> loadSync(List<FXPath> loadedPaths) {

        if (isRoot()) {
            listRoots(loadedPaths);
        } else {
            list(loadedPaths);
        }

        setLoaded(true);

        return loadedPaths;
    }

    private void listRoots(List<FXPath> loadedPaths) {
        try (var stream = StreamSupport.stream(FileSystems.getDefault().getRootDirectories().spliterator(), false)) {
            stream.forEach(p -> {
                var pd = getFromCache(this, p, true);
                loadedPaths.add(pd);
            });
        }
    }

    private void list(List<FXPath> loadedPaths) {
        try (var stream = Files.newDirectoryStream(getPath())) {
            var iterator = stream.iterator();

            while (iterator.hasNext()) {
                var p = iterator.next();
                boolean directory = Files.isDirectory(p);
                var pd = getFromCache(this, p, directory);
                loadedPaths.add(pd);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static FXPath addInParent(FXPath parent, Path p, boolean directory) {
        var pd = getFromCache(parent, p, directory);

        if (parent.isLoaded()) {
            parent.paths.add(pd);
        }

        parent.setDirLeaf(!directory);
        parent.setLeaf(false);

        return pd;
    }

    private static FXPath getFromCache(FXPath parent, Path path, boolean dir) {

        var pd = getFromCache(path, k -> new WeakReference<>(new FXPath(parent, path, dir)));

        if (parent != null) {
            pd.parents.add(parent);
        }

        return pd;
    }

    private static FXPath getFromCache(Path path, Function<Path, WeakReference<FXPath>> function) {

        var fxpath = CACHE.computeIfAbsent(path, function).get();

        if (fxpath == null) {
            var ref = function.apply(path);
            fxpath = ref.get();
            CACHE.put(path, ref);
        }

        fxpath.watch();

        return fxpath;
    }

    private static FXPath putToCache(Path path, FXPath fxpath) {

        return getFromCache(path, p -> new WeakReference<>(fxpath));
    }

    private static FXPath getFromCache(Path path) {

        var ref = CACHE.get(path);
        var fxpath = ref != null ? ref.get() : null;

        return fxpath;
    }

    private static void removeFromCache(Path path) {
        CACHE.remove(path);
    }

    private void watchDirectory(List<WatchEvent<?>> events) {
        getLock().lock();
        try {
            events.forEach(e -> {

                if (e.context() instanceof Path) {
                    var contextPath = getPath().resolve((Path) e.context());

                    if (e.kind() == StandardWatchEventKinds.ENTRY_CREATE) {
                        if (paths.stream().noneMatch(p -> p.getPath().equals(contextPath))) {
                            addInParent(this, contextPath, Files.isDirectory(contextPath));
                        }
                    } else if (e.kind() == StandardWatchEventKinds.ENTRY_MODIFY) {
                        var fxpath = getFromCache(contextPath);
                        try {
                            if (fxpath != null && Files.exists(contextPath)
                                    && fxpath.basicFileAttributes.getLastModifiedTime() != Files.getLastModifiedTime(contextPath).toMillis()) {
                                fxpath.setFileAttributes();
                                fxpath.onModified.forEach(c -> c.accept(fxpath));
                            }
                        } catch (IOException ex) {
                            throw new RuntimeException(ex);
                        }
                    } else if (e.kind() == StandardWatchEventKinds.ENTRY_DELETE) {
                        var fxpath = getFromCache(contextPath);

                        if (fxpath != null) {
                            fxpath.deleteExternally();
                        }
                    }
                }
            });
        } finally {
            getLock().unlock();
        }
    }

    public FXBasicFileAttributes getBasicFileAttributes() {
        return basicFileAttributes;
    }

    public void setFileAttributes() {
        basicFileAttributes = new FXBasicFileAttributes(this);
    }

    void saved(Path newPath) {
        if (!getPath().equals(newPath)) {
            removeFromCache(getPath());
            setPath(newPath);
            putToCache(newPath, this);
        }

        setFileAttributes();
        onModified.forEach(c -> c.accept(this));
    }

    void search(PathMatcher pathMatcher, Pattern textRegex, Consumer<FilePosition> consumer, AtomicBoolean stop) {

        if (!isReadable() || stop.get()) {
            return;
        }

        if (isFile()) {
            searchFile(pathMatcher, textRegex, consumer, stop);
        } else {

            List<FXPath> loadedPaths = isLoaded() ? paths : loadSync(paths);

            loadedPaths.parallelStream().sorted(Comparator.reverseOrder()).forEach(p -> p.search(pathMatcher, textRegex, consumer, stop));
        }
    }

    private void searchFile(PathMatcher pathNatcher, Pattern textRegex, Consumer<FilePosition> consumer, AtomicBoolean stop) {
        if (stop.get()) {
            return;
        }
        Path fileName = getPath().getFileName();
        if (fileName != null && pathNatcher.matches(fileName)) {
            PathFilePosition pathPointer = new PathFilePosition(this);
            if (textRegex == null) {
                consumer.accept(pathPointer);
            } else {
                find(pathPointer, textRegex, stop);
                if (!pathPointer.getStringFilePositions().isEmpty()) {
                    consumer.accept(pathPointer);
                }
            }
        }
    }

    private void find(PathFilePosition pathPointer, Pattern pattern, AtomicBoolean stop) {
        try {
            if (Files.probeContentType(getPath()).toLowerCase().startsWith("text")) {

                try (var lines = Files.lines(getPath())) {
                    Searcher.get().search(lines, pattern, sr -> {
                        pathPointer.add(new StringFilePosition(sr));
                        return !stop.get();
                    });
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, e.getMessage(), e);
        }
    }

    @Override
    public String toString() {
        return getName();
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
            result = getName().compareToIgnoreCase(o.getName());
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

package dev.jfxde.j.nio.file;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.Watchable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;

public class WatchServiceRegister {

    private Map<Watchable, List<WeakReference<Consumer<List<WatchEvent<?>>>>>> register = Collections.synchronizedMap(new WeakHashMap<>());
    private Map<Watchable, WatchKey> watchKeys = new WeakHashMap<>();
    private Map<Watchable, WeakReference<Path>> sharedPaths = new WeakHashMap<>();
    private WatchService watchService;
    private volatile boolean started;

    public synchronized Path register(Path path, Consumer<List<WatchEvent<?>>> consumer) {

        var sharedPathRef = sharedPaths.get(path);
        Path sharedPath = sharedPathRef == null ? null : sharedPathRef.get();

        if (sharedPath == null) {
            sharedPaths.put(path, new WeakReference<>(path));
            sharedPath = path;
        }

        try {
            if (!watchKeys.containsKey(sharedPath)) {
                WatchKey watchKey = sharedPath.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                watchKeys.put(sharedPath, watchKey);
            }

            if (sharedPathRef == null || sharedPathRef.get() == null || sharedPath != path) {
                register.computeIfAbsent(sharedPath, k -> new ArrayList<>()).add(new WeakReference<>(consumer));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sharedPath;
    }

    public synchronized void unwatch(Path path) {
        var key = watchKeys.remove(path);
        if (key != null) {
            key.cancel();
        }
    }

    public void start() {
        try {
            started = true;
            watchService = FileSystems.getDefault().newWatchService();

            Thread thread = new Thread(() -> {
                while (started) {
                    WatchKey key = null;
                    try {
                        key = watchService.take();
                        // Thread.sleep(1000);
                    } catch (InterruptedException | ClosedWatchServiceException e) {
                        break;
                    }

                    if (key == null) {
                        continue;
                    }

                    var events = key.pollEvents();
                    var consumers = register.getOrDefault(key.watchable(), List.of());
                    consumers.stream()
                            .map(r -> r.get())
                            .filter(c -> c != null)
                            .forEach(c -> c.accept(events));

                    boolean valid = key.reset();
                    if (!valid) {
                        break;
                    }
                }
            });

            thread.setDaemon(true);
            thread.start();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            started = false;
            if (watchService != null) {
                watchService.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}

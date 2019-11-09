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

    private Map<Watchable, List<WeakReference<Consumer<List<WatchEvent<?>>>>>> register =  Collections.synchronizedMap(new WeakHashMap<>());
    private Map<Watchable, WatchKey> keys = new WeakHashMap<>();
    private Map<Watchable, Path> paths = new WeakHashMap<>();
    private WatchService watchService;
    private volatile boolean started;

    public synchronized Path register(Path path, Consumer<List<WatchEvent<?>>> consumer) {

        Path sharedPath = paths.get(path);
        try {
            if (!register.containsKey(path)) {
                WatchKey key = path.register(watchService, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
                        StandardWatchEventKinds.ENTRY_MODIFY);
                keys.put(path, key);
                paths.put(path, path);
            }

            if (sharedPath != path) {
                register.computeIfAbsent(path, k -> new ArrayList<>()).add(new WeakReference<>(consumer));
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return sharedPath;
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
                        Thread.sleep(50);
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

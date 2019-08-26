package dev.jfxde.logic;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;

import javafx.application.Platform;

public class FileLocker {

    private FileChannel lockFileChannel;
    private FileLock fileLock;
    private WatchService watcher;
    private final Path lockFilePath;
    private final Path messageFilePath;

    public FileLocker(Path lockFilePath, Path messagePath) {
        this.lockFilePath = lockFilePath;
        this.messageFilePath = messagePath;
    }

    void lock() throws IOException {
        try {
            lockFileChannel = FileChannel.open(lockFilePath,
                    StandardOpenOption.CREATE,
                    StandardOpenOption.WRITE);
            fileLock = lockFileChannel.tryLock();
            if (fileLock == null) {
                lockFileChannel.close();
                Files.deleteIfExists(messageFilePath);
                // Delete the file from the other application in case
                // the watcher uses polling and needs some time to detect the
                // message file.
                Files.createFile(messageFilePath);
                throw new IOException("Platform already started.");
            }
        } finally {
        }
    }

    void watch(Runnable messageHandler) throws IOException {
        watcher = FileSystems.getDefault().newWatchService();

        Path messageDirPath = messageFilePath.getParent();
        WatchKey regKey = messageDirPath.register(watcher, StandardWatchEventKinds.ENTRY_CREATE);

        Thread thread = new Thread(() -> {
            while (true) {
                WatchKey key = null;
                try {
                	key = watcher.take();
                } catch (InterruptedException|ClosedWatchServiceException e) {
                    break;
                }

                if (key != regKey) {
                    continue;
                }

                if (key.pollEvents().stream().anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE &&
                        messageDirPath.resolve(((Path) e.context())).equals(messageFilePath))) {
                    try {
                        Files.deleteIfExists(messageFilePath);
                    } catch (IOException e) {
                    }

                    Platform.runLater(messageHandler);
                }

                boolean valid = key.reset();
                if (!valid) {
                    break;
                }
            }
        });

        thread.setDaemon(true);
        thread.start();
    }

    void stop() throws IOException {
        watcher.close();
        fileLock.release();
        lockFileChannel.close();
        Files.delete(lockFilePath);
    }
}

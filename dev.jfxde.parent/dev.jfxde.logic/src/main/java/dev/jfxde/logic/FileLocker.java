package dev.jfxde.logic;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.StandardWatchEventKinds;

import dev.jfxde.j.nio.file.WatchServiceRegister;
import javafx.application.Platform;

public class FileLocker {

    private FileChannel lockFileChannel;
    private FileLock fileLock;
    private final Path lockFilePath;
    private final Path messageFilePath;
    private Path messageDirPath;

    public FileLocker(Path lockFilePath, Path messagePath) {
        this.lockFilePath = lockFilePath;
        this.messageFilePath = messagePath;
        this.messageDirPath = messageFilePath.getParent();
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

    void watch(Runnable messageHandler, WatchServiceRegister watchServiceRegister) throws IOException {

        messageDirPath = watchServiceRegister.register(messageDirPath, events -> {
            if (events.stream().anyMatch(e -> e.kind() == StandardWatchEventKinds.ENTRY_CREATE &&
                    messageDirPath.resolve(((Path) e.context())).equals(messageFilePath))) {
                try {
                    Files.deleteIfExists(messageFilePath);
                } catch (IOException e) {
                }

                Platform.runLater(messageHandler);
            }
        });
    }

    void stop() throws IOException {
        fileLock.release();
        lockFileChannel.close();
        Files.delete(lockFilePath);
    }
}

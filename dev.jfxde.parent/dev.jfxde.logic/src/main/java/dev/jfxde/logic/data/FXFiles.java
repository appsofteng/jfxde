package dev.jfxde.logic.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.jfxde.j.nio.file.XFiles;

public final class FXFiles {

    private FXFiles() {
    }

    public static FXPath createDirectory(FXPath parent, String name) {
        FXPath pathDescriptor = null;
        FXPath.getLock().lock();
        try {
            Path path = XFiles.createDirectory(parent.getPath(), name);

            pathDescriptor = FXPath.createDirectory(parent, path);
        } finally {
            FXPath.getLock().unlock();
        }

        return pathDescriptor;
    }

    public static FXPath createFile(FXPath parent, String name) {
        FXPath pathDescriptor = null;
        FXPath.getLock().lock();
        try {
            Path path = XFiles.createFile(parent.getPath(), name);

            pathDescriptor = FXPath.createFile(parent, path);
        } finally {
            FXPath.getLock().unlock();
        }
        return pathDescriptor;
    }

    public static CompletableFuture<Void> move(List<FXPath> pds, FXPath targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            FXPath.getLock().lock();
            try {
                pds.forEach(pd -> {
                    Path target = XFiles.move(pd.getPath(), targetDir.getPath());
                    pd.move(targetDir, target);
                });
            } finally {
                FXPath.getLock().unlock();
            }
        });
        return future;
    }

    public static CompletableFuture<Void> copy(List<FXPath> pds, FXPath targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            FXPath.getLock().lock();
            try {
                pds.forEach(pd -> {
                    Path target = XFiles.copy(pd.getPath(), targetDir.getPath());
                    FXPath.copy(targetDir, target);
                });
            } finally {
                FXPath.getLock().unlock();
            }
        });
        return future;
    }

    public static CompletableFuture<Void> delete(List<FXPath> pds) {

        var future = CompletableFuture.runAsync(() -> {
            FXPath.getLock().lock();
            try {
                pds.forEach(pd -> {
                    XFiles.delete(pd.getPath());
                    pd.delete();
                });
            } finally {
                FXPath.getLock().unlock();
            }
        });

        return future;
    }

    public static boolean rename(FXPath pd, String name) {
        boolean renamed = false;
        var targetPath = pd.getPath().resolveSibling(name);
        if (Files.notExists(targetPath)) {
            FXPath.getLock().lock();
            try {
                Files.move(pd.getPath(), targetPath);
                pd.rename(targetPath, name);
                renamed = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            } finally {
                FXPath.getLock().unlock();
            }
        }
        return renamed;
    }

    public static CompletableFuture<Void> save(FXPath path, String string) {

        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            FXPath.getLock().lock();
            try {
                Path newPath = XFiles.save(path.getPath(), string);
                path.saved(newPath);
            } finally {
                FXPath.getLock().unlock();
            }
        });

        return future;
    }
}

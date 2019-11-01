package dev.jfxde.logic.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.jfxde.jfxext.nio.file.FileUtils;

public final class PathDescriptors {

    private PathDescriptors() {
    }

    public static PathDescriptor createDirectory(PathDescriptor parent, String name) {

        Path path = FileUtils.createDirectory(parent.getPath(), name);

        PathDescriptor pathDescriptor = PathDescriptor.createDirectory(parent, path);

        return pathDescriptor;
    }

    public static PathDescriptor createFile(PathDescriptor parent, String name) {

        Path path = FileUtils.createFile(parent.getPath(), name);

        PathDescriptor pathDescriptor = PathDescriptor.createFile(parent, path);

        return pathDescriptor;
    }

    public static CompletableFuture<Void> move(List<PathDescriptor> pds, PathDescriptor targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                Path target = FileUtils.move(pd.getPath(), targetDir.getPath());
                pd.move(targetDir, target);
            });
        });
        return future;
    }

    public static CompletableFuture<Void> copy(List<PathDescriptor> pds, PathDescriptor targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                Path target = FileUtils.copy(pd.getPath(), targetDir.getPath());
                pd.copy(targetDir, target);
            });
        });
        return future;
    }

    public static CompletableFuture<Void> delete(List<PathDescriptor> pds) {

        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                FileUtils.delete(pd.getPath());
                pd.delete();
            });
        });

        return future;
    }

    public static boolean rename(PathDescriptor pd, String name) {
        boolean renamed = false;
        var targetPath = pd.getPath().resolveSibling(name);
        if (Files.notExists(targetPath)) {
            try {
                Files.move(pd.getPath(), targetPath);
                pd.rename(targetPath, name);
                renamed = true;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return renamed;
    }
}

package dev.jfxde.logic.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static CompletableFuture<Void> delete(PathDescriptor pd) {

        return FileUtils.delete(pd.getPath()).thenRun(() -> pd.delete());
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

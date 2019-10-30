package dev.jfxde.logic.data;

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
}

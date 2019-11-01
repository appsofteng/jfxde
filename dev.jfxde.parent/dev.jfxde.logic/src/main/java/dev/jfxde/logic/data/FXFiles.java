package dev.jfxde.logic.data;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import dev.jfxde.j.nio.file.XFiles;

public final class FXFiles {

    private FXFiles() {
    }

    public static FXPath createDirectory(FXPath parent, String name) {

        Path path = XFiles.createDirectory(parent.getPath(), name);

        FXPath pathDescriptor = FXPath.createDirectory(parent, path);

        return pathDescriptor;
    }

    public static FXPath createFile(FXPath parent, String name) {

        Path path = XFiles.createFile(parent.getPath(), name);

        FXPath pathDescriptor = FXPath.createFile(parent, path);

        return pathDescriptor;
    }

    public static CompletableFuture<Void> move(List<FXPath> pds, FXPath targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                Path target = XFiles.move(pd.getPath(), targetDir.getPath());
                pd.move(targetDir, target);
            });
        });
        return future;
    }

    public static CompletableFuture<Void> copy(List<FXPath> pds, FXPath targetDir) {
        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                Path target = XFiles.copy(pd.getPath(), targetDir.getPath());
                FXPath.copy(targetDir, target);
            });
        });
        return future;
    }

    public static CompletableFuture<Void> delete(List<FXPath> pds) {

        var future = CompletableFuture.runAsync(() -> {
            pds.forEach(pd -> {
                XFiles.delete(pd.getPath());
                pd.delete();
            });
        });

        return future;
    }

    public static boolean rename(FXPath pd, String name) {
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

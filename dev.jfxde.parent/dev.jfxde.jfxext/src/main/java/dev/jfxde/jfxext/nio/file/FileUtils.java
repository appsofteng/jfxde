package dev.jfxde.jfxext.nio.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jfxde.jfxext.util.LU;

public final class FileUtils {

    private FileUtils() {
    }

    public static Path createFile(Path path, String name) {
        Path result = getUniquePath(path, name);
        try {
            result = Files.createFile(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static Path createDirectory(Path path, String name) {
        Path result = getUniquePath(path, name);
        try {
            result = Files.createDirectory(result);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    private static Path getUniquePath(Path path, String name) {
        Path result = null;
        if (!Files.isDirectory(path)) {
            path = path.getParent();
        }

        result = path.resolve(name);
        int i = 1;

        while (Files.exists(result)) {
            result = path.resolve(String.format("%s (%d)", name, i++));
        }

        return result;
    }

    public static CompletableFuture<Void> delete(Path path) {

        var future = CompletableFuture.runAsync(() -> {
            if (Files.isDirectory(path)) {
                deleteDirectory(path);
            } else {
                LU.of(() -> Files.delete(path));
            }
        });

        return future;
    }

    private static void deleteDirectory(Path path) {

        try (Stream<Path> stream = Files.walk(path)) {
            List<Path> paths = stream
                    .sorted(Comparator.reverseOrder())
                    .collect(Collectors.toList());
            paths.forEach(p -> LU.of(() -> Files.delete(p)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean hasSubDirs(Path dir) {
        boolean result = false;

        try (var stream = Files.list(dir)) {

            result = stream.filter(p -> Files.isDirectory(p))
                    .filter(p -> Files.isReadable(p))
                    .findAny().isPresent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public static boolean isEmpty(Path dir) {
        boolean result = false;

        try (var stream = Files.list(dir)) {

            result = stream
                    .filter(p -> Files.isReadable(p))
                    .findAny().isPresent();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }
}

package dev.jfxde.j.nio.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

import dev.jfxde.j.util.LU;

public final class XFiles {

    private XFiles() {
    }

    public static String getFileExtension(Path path) {
        Path fileName = path.getFileName();
        String extension = null;

        if (fileName != null) {
            String name = fileName.toString().toLowerCase();
            int i = name.lastIndexOf(".") + 1;
            if (i > 0) {
                extension = name.substring(i);
            }
        }

        return extension;
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

    public static Path move(Path source, Path targetDir) {
        Path target = getUniquePath(targetDir, source.getFileName().toString());
        try {
            if (Files.isDirectory(source)) {
                moveDirectory(source, target);
            } else {
                target = Files.move(source, target);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return target;
    }

    public static Path copy(Path source, Path targetDir) {
        Path target = getUniquePath(targetDir, source.getFileName().toString());
        try {
            if (Files.isDirectory(source)) {
                target = copyDirectory(source, target);
            } else {
                target = Files.copy(source, target);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return target;
    }

    private static Path moveDirectory(Path source, Path target) {
        target = copyDirectory(source, target);
        deleteDirectory(source);
        return target;
    }

    private static Path copyDirectory(Path source, Path target) {
        try (var stream = Files.walk(source)) {
            stream.forEach(p -> {
                Path entryTarget = target.resolve(source.relativize(p));
                try {
                    Files.copy(p, entryTarget);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return target;
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

    public static void delete(Path path) {

        if (Files.isDirectory(path)) {
            deleteDirectory(path);
        } else {
            LU.of(() -> Files.delete(path));
        }
    }

    private static void deleteDirectory(Path path) {

        try (Stream<Path> stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(p -> LU.of(() -> Files.delete(p)));
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

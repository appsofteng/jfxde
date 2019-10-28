package dev.jfxde.jfxext.nio.file;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {

    private FileUtils() {}

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
}

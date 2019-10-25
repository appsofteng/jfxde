package dev.jfxde.logic;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ForkJoinPool;

import javax.json.bind.Jsonb;
import javax.json.bind.JsonbBuilder;
import javax.json.bind.JsonbConfig;
import javax.json.bind.JsonbException;

public final class JsonUtils {

    private static final Jsonb JSONB = JsonbBuilder.create(new JsonbConfig().withFormatting(true));

    private JsonUtils() {
    }

    public static void toJson(Object obj, Path path, String file) {

        Runnable task = () -> {
            synchronized (file) {
                try (var f = Files.newBufferedWriter(path.resolve(file))) {
                    JSONB.toJson(obj, f);

                } catch (JsonbException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        ForkJoinPool.commonPool().execute(task);
    }

    public static <T> T fromJson(Path path, String file, Type type, T defaultObj) {

        T result = defaultObj;
        Path fullPath = path.resolve(file);
        if (Files.exists(fullPath)) {
            synchronized (file) {
                try (var f = Files.newBufferedReader(fullPath)) {

                    result = JSONB.fromJson(f, type);

                } catch (JsonbException | IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return result;
    }
}

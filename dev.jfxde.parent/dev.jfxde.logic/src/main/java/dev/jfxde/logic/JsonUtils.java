package dev.jfxde.logic;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public abstract class JsonUtils {

    private JsonUtils() {
    }

    private static Gson getGson() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        return gson;
    }

    public static void toJson(Object obj, Path file) {
        Gson gson = getGson();
        try (var f = Files.newBufferedWriter(file)) {
            gson.toJson(obj, f);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(Path file, Class<T> type, T defaultObj) {
        T obj = defaultObj;

        if (Files.exists(file)) {
            Gson gson = getGson();

            try (var f = Files.newBufferedReader(file)) {
                obj = gson.fromJson(f, type);
            } catch (JsonIOException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }

    public static <T> T fromJson(Path file, Type type, T defaultObj) {
        T obj = defaultObj;

        if (Files.exists(file)) {
            Gson gson = getGson();

            try (var f = Files.newBufferedReader(file)) {
                obj = gson.fromJson(f, type);
            } catch (JsonIOException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }
}

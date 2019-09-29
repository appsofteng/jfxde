package dev.jfxde.logic;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;

public abstract class JsonUtils {

    private JsonUtils() {
    }

    public static void toJson(Object obj, Path file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (var f = Files.newBufferedWriter(file)) {
            gson.toJson(obj, f);
        } catch (JsonIOException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T fromJson(Path file, Class<T> type, T defaultObj) {
        T obj = defaultObj;

        if (Files.exists(file)) {
            Gson gson = new Gson();

            try (var f = Files.newBufferedReader(file)) {
                obj = gson.fromJson(f, type);
            } catch (JsonIOException | IOException e) {
                throw new RuntimeException(e);
            }
        }

        return obj;
    }
}

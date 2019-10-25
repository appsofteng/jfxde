package dev.jfxde.api;

import java.lang.reflect.Type;

public interface DataController {

    void toJson(Object obj, String relativeFilePath);
    <T> T fromJson(String relativeFilePath, Type type, T defaultObj);
}

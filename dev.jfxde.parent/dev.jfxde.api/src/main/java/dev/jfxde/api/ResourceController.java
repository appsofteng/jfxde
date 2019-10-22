package dev.jfxde.api;

import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;

public interface ResourceController {

    String getString(String key, Object... args);

    String getStringOrDefault(String key, String defaultValue, Object... args);

    String getStringMaxWidth(String key, String arg, int maxWidth);

    void put(StringProperty property, String key, Object... args);

    StringBinding getStringBinding(String key, Object... args);

    StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args);

    String getCss(String name);

    Map<String, String> getStrings(Set<String> keys);

    ResourceBundle getStringBundle();
}

package dev.jfxde.api;

import java.util.Map;
import java.util.Set;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface ResourceController {

	String getString(String key, Object... args);
	StringBinding getStringBinding(String key, Object... args);
	StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args);
	String getStringMaxWidth(String key, String arg, int maxWidth);
	String getCss(String name);
	Map<String,String> getStrings(Set<String> keys);
}

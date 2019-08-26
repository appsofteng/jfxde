package dev.jfxde.api;

import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface ResourceController {

	String getText(String key, Object... args);
	StringBinding getTextBinding(String key, Object... args);
	StringBinding getTextBinding(ReadOnlyObjectProperty<?> key, Object... args);
	String getTextMaxWidth(String key, String arg, int maxWidth);
	String getCss(String name);
}

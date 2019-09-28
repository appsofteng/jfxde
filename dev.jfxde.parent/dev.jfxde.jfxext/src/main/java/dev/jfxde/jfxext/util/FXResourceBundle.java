package dev.jfxde.jfxext.util;

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class FXResourceBundle {
    private static ObjectProperty<Locale> locale = new SimpleObjectProperty<>();

    private static final String DEFAULT_BUNDLE_NAME = "strings";
    private static final String DEFAULT_BASE_NAME = FXResourceBundle.class.getPackageName() + "." + DEFAULT_BUNDLE_NAME;
    private String baseName;
    private Module module;
    private FXResourceBundle parent;

    private static Map<String,FXResourceBundle> cache = new ConcurrentHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(FXResourceBundle.class.getName());

    private FXResourceBundle(String baseName, Module module, FXResourceBundle parent) {
        this.baseName = baseName;
        this.module = module;
        this.parent = parent;

        cache.put(baseName, this);
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(String value) {
        locale.set(Locale.forLanguageTag(value));
    }

    public static FXResourceBundle getDefaultBundle() {
        return getBundle​(DEFAULT_BASE_NAME, null, null);
    }

    public static FXResourceBundle getBundle​(String baseName) {
        return getBundle​(baseName, null, null);
    }

    public static FXResourceBundle getBundle​(String baseName, Module module) {

        return getBundle​(baseName, module, null);
    }

    public static FXResourceBundle getBundle​(String baseName, Module module, FXResourceBundle parent) {

        FXResourceBundle bundle = cache.merge(baseName, new FXResourceBundle(baseName, module, parent), (o,n) -> o);

        return bundle;
    }

    private ResourceBundle getBundle() {

        ResourceBundle bundle = AccessController.doPrivileged((PrivilegedAction<ResourceBundle>) () -> {
            if (module == null) {
                return ResourceBundle.getBundle(baseName, locale.get());
            } else {
                return ResourceBundle.getBundle(baseName, locale.get(), module);
            }
        });

        return bundle;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(String key) {
        Object value = null;

        try {

            value = getBundle().getObject(key);

        } catch (MissingResourceException e) {
            if (parent != null) {
                value = parent.getObject(key);
            } else {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }

        return (T) value;
    }

    public String getString​(String key, Object... args) {

        String value = getObject(key);

        if (value == null) {
            value = key;
        }

        return MessageFormat.format(value, args);
    }

    public StringBinding getStringBinding(String key, Object... args) {
        return Bindings.createStringBinding(() -> getString​(key, args), locale);
    }

    public StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args) {
        return Bindings.createStringBinding(
                () -> getString​(key.getValue().toString().toLowerCase(), args), key, locale);
    }

    public String getStringOrDefault(String key, String defaultValue, Object... args) {
        String value = defaultValue;
        try {
            value = getString​(key, args);
        } catch (MissingResourceException e) {

        }

        return value;
    }

    public String getStringtMaxWidth(String key, String arg, int maxWidth) {
        arg = arg.replaceAll("[\\n\\r]+", " ");
        String text = String.format("%." + maxWidth + "s%s", arg, arg.length() > maxWidth ? "..." : "");

        return getString​(key, text);
    }
}

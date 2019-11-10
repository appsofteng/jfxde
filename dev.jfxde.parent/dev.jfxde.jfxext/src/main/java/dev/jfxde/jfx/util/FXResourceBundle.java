package dev.jfxde.jfx.util;

import java.io.File;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.StringProperty;

public class FXResourceBundle {
    private static ObjectProperty<Locale> locale = new SimpleObjectProperty<>();

    private static final String BUNDLE_DIR_NAME = "bundles";
    private static final String BUNDLE_FILE_NAME = "strings";

    private final String bundleDir;
    private Class<?> caller;
    private String baseName;
    private Module module;
    private FXResourceBundle parent;

    private static Map<String, FXResourceBundle> cache = new ConcurrentHashMap<>();

    private static final Logger LOGGER = Logger.getLogger(FXResourceBundle.class.getName());

    private Map<StringProperty, List<Object>> stringProperties = new WeakHashMap<>();

    private FXResourceBundle(Class<?> caller, String baseName, FXResourceBundle parent) {
        this.caller = caller;
        this.baseName = baseName;
        this.module = caller.getModule();
        this.parent = parent;

        String path = caller.getPackageName().replace(".", "/");
        this.bundleDir = path + "/" + BUNDLE_DIR_NAME + "/";

        locale.addListener((v, o, n) -> {
            if (n != null) {
                stringProperties.keySet().forEach(k -> {
                    var s = stringProperties.get(k);
                    k.set(getString​(s.get(0).toString(), (Object[])s.get(1)));
                });
            }
        });
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(String value) {
        locale.set(Locale.forLanguageTag(value));
    }

    public static FXResourceBundle getBundle() {
        return getBundle​(FXResourceBundle.class, null);
    }

    public static FXResourceBundle getBundle​(Class<?> caller) {
        return getBundle​(caller, getBundle());
    }

    private static FXResourceBundle getBundle​(Class<?> caller, FXResourceBundle parent) {
        String baseName = caller.getPackageName() + "." + BUNDLE_DIR_NAME + "." + BUNDLE_FILE_NAME;
        FXResourceBundle bundle = cache.computeIfAbsent(baseName, k -> new FXResourceBundle(caller, baseName, parent));

        return bundle;
    }

    public ResourceBundle getResourceBundle() {

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

            value = getResourceBundle().getObject(key);

        } catch (MissingResourceException e) {
            if (parent != null) {
                value = parent.getObject(key);
            } else {
                LOGGER.log(Level.INFO, e.getMessage(), e);
            }
        }

        return (T) value;
    }

    public String getString​(String key, Object... args) {

        return getStringOrDefault(key, key, args);
    }

    public void put(StringProperty property, String key, Object... args) {
        property.set(getString​(key, args));
        stringProperties.put(property, List.of(key, args));
    }

    public StringBinding getStringBinding(String key, Object... args) {
        return Bindings.createStringBinding(() -> getString​(key, args), locale);
    }

    public StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args) {
        return Bindings.createStringBinding(
                () -> getString​(key.getValue().toString().toLowerCase(), args), key, locale);
    }

    public String getStringOrDefault(String key, String defaultValue, Object... args) {
        String value = getObject(key);

        if (value == null) {
            value = defaultValue;
        }

        return MessageFormat.format(value, args);
    }

    public String getStringMaxWidth(String key, String arg, int maxWidth) {
        arg = arg.replaceAll("[\\n\\r]+", " ");
        String text = String.format("%." + maxWidth + "s%s", arg, arg.length() > maxWidth ? "..." : "");

        return getString​(key, text);
    }

    public Set<String> getLocales() {
        Set<String> locales = Set.of();

        try {

            final File jarFile = new File(caller.getProtectionDomain().getCodeSource().getLocation().getPath());

            if (jarFile.isFile()) {
                final JarFile jar = new JarFile(jarFile);

                locales = jar.stream()
                        .map(JarEntry::getName)
                        .filter(n -> n.startsWith(bundleDir))
                        .filter(n -> n.length() > bundleDir.length())
                        .map(n -> n.substring(bundleDir.length(), n.lastIndexOf(".")))
                        .filter(n -> n.contains("_"))
                        .map(n -> n.substring(n.indexOf("_") + 1).replace("_", "-"))
                        .collect(Collectors.toSet());

                jar.close();
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return locales;
    }
}

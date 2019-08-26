package dev.jfxde.logic;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

import java.io.File;
import java.io.InputStream;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import dev.jfxde.api.ResourceController;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public final class ResourceManager extends Manager implements ResourceController {

    public static final double SMALL_ICON_SIZE = 16;
    public static final double MEDIUM_ICON_SIZE = 32;

    private static final String BUNDLE_DIR_NAME = "bundles";
    private static final String BUNDLE_FILE_NAME = "messages";
    private final String bundleDir;
    private final String bundleBaseName;

    private static ObjectProperty<Locale> locale = new SimpleObjectProperty<>();
    private Class<?> caller;
    private Map<String, Image> iconImages = new HashMap<>();
    private String iconAltText = "";
    private ResourceManager parent;

    public ResourceManager(Class<?> caller) {
        this(caller, "", null);
    }

    public ResourceManager(Class<?> caller, String iconAltText, ResourceManager parent) {
        this.caller = caller;
        this.iconAltText = iconAltText;
        this.parent = parent;

        this.bundleDir = caller.getPackageName().replace(".", "/") + "/" + BUNDLE_DIR_NAME + "/";
        this.bundleBaseName = caller.getPackageName() + "." + BUNDLE_DIR_NAME + "." + BUNDLE_FILE_NAME;
    }

    public static Locale getLocale() {
        return locale.get();
    }

    public static void setLocale(String locale) {
        ResourceManager.locale.set(Locale.forLanguageTag(locale));
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
            Sys.em().log(e);
        }

        return locales;
    }

    private String getString(String key) {
        String value = key;

        try {

            value = AccessController.doPrivileged((PrivilegedAction<String>) () -> {
                String res = ResourceBundle
                        .getBundle(bundleBaseName, getLocale(), caller.getModule())
                        .getString(key);
                return res;
            });

        } catch (MissingResourceException e) {
            if (parent != null) {
                value = parent.getString(key);
            }
        }

        return value;
    }

    public String getText(String key, Object... args) {
        return MessageFormat.format(getString(key), args);
    }

    public String getTextOrDefault(String key, String defaultValue, Object... args) {
        String value = defaultValue;
        try {
            value = getString(key);
        } catch (MissingResourceException e) {

        }

        return MessageFormat.format(value, args);
    }

    public String getTextMaxWidth(String key, String arg, int maxWidth) {
        arg = arg.replaceAll("[\\n\\r]+", " ");
        String text = String.format("%." + maxWidth + "s%s", arg, arg.length() > maxWidth ? "..." : "");

        return MessageFormat.format(getString(key), text);
    }

    public StringBinding getTextBinding(String key, Object... args) {
        return Bindings.createStringBinding(() -> MessageFormat.format(getString(key), args), locale);
    }

    public StringBinding getTextBinding(ReadOnlyObjectProperty<?> key, Object... args) {
        return Bindings.createStringBinding(
                () -> MessageFormat.format(getString(key.getValue().toString().toLowerCase()), args), key, locale);
    }

    public String getCss(String name) {
        return caller.getResource("css/" + name + ".css").toExternalForm();
    }

    public Region getMediumIcon(String style) {

        return getIcon("medium", iconAltText, MEDIUM_ICON_SIZE, style);
    }

    public Region getSmallIcon(String style) {

        return getIcon("small", iconAltText, SMALL_ICON_SIZE, style);
    }

    private Region getIcon(String name, String iconAltText, double size, String style) {
        Label icon = new Label();

        Image image = getIconImage(name);

        if (image == null) {
            icon.setText(iconAltText);
            icon.setContentDisplay(ContentDisplay.TEXT_ONLY);
            icon.getStyleClass().add(style);
        } else {
            icon.setGraphic(new ImageView(image));
            icon.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
        }

        icon.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        icon.setPrefSize(size, size);
        icon.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        return icon;
    }

    private Image getIconImage(String name) {

        Image image = null;

        if (!iconImages.containsKey(name)) {
            String imgPath = "icons/" + name + ".png";
            InputStream is = caller.getResourceAsStream(imgPath);

            if (is != null) {
                image = new Image(is);
            }
            iconImages.put(name, image);
        } else {
            image = iconImages.get(name);
        }

        return image;
    }
}

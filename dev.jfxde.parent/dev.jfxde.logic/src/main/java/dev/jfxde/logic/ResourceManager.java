package dev.jfxde.logic;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

import dev.jfxde.api.ResourceController;
import dev.jfxde.jfxext.util.FXResourceBundle;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;

public final class ResourceManager extends Manager implements ResourceController {

    private static final String BUNDLE_DIR_NAME = "bundles";
    private static final String BUNDLE_FILE_NAME = "strings";

    private final String bundleDir;
    private final String bundleBaseName;

    public static final double SMALL_ICON_SIZE = 16;
    public static final double MEDIUM_ICON_SIZE = 32;
    private static final String SMALL_ICON_NAME = "small";
    private static final String MEDIUM_ICON_NAME = "medium";
    private static final String ICON_DIR_NAME = "icons";
    private Map<String, Image> iconImages = new HashMap<>();
    private String iconAltText = "";

    private static final String CSS_DIR_NAME = "css";
    private static final String CSS_FILE_NAME = "style";

    private Class<?> caller;
    private ResourceManager parent;

    public ResourceManager(Class<?> caller) {
        this(caller, "", null);
    }

    public ResourceManager(Class<?> caller, String iconAltText, ResourceManager parentCaller) {
        this.caller = caller;
        this.iconAltText = iconAltText;
        this.parent = parentCaller;

        String path = caller.getPackageName().replace(".", "/");
        this.bundleDir = path + "/" + BUNDLE_DIR_NAME + "/";
        this.bundleBaseName = caller.getPackageName() + "." + BUNDLE_DIR_NAME + "." + BUNDLE_FILE_NAME;
    }

    public static Locale getLocale() {
        return FXResourceBundle.getLocale();
    }

    public static void setLocale(String locale) {
        FXResourceBundle.setLocale(locale);
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

    private FXResourceBundle getBundle() {

        FXResourceBundle parentBundle = null;

        if (parent != null) {
            parentBundle = parent.getBundle();
        }

        FXResourceBundle resourceBundle = parentBundle;

        try {
            resourceBundle = FXResourceBundle.getBundle​(bundleBaseName, caller.getModule(), parentBundle);
        } catch (MissingResourceException e) {

        }

        return resourceBundle;
    }

    @Override
    public ResourceBundle getStringBundle() {
        return getBundle().getResourceBundle();
    }

    @Override
    public String getString(String key, Object... args) {
        return getBundle().getString​(key, args);
    }

    @Override
    public String getStringOrDefault(String key, String defaultValue, Object... args) {
        return getBundle().getStringOrDefault(key, defaultValue, args);
    }

    @Override
    public String getStringMaxWidth(String key, String arg, int maxWidth) {
        return getBundle().getStringtMaxWidth(key, arg, maxWidth);
    }

    @Override
    public void put(StringProperty property, String key, Object... args) {
        getBundle().put(property, key, args);
    }

    @Override
    public StringBinding getStringBinding(String key, Object... args) {
        return getBundle().getStringBinding(key, args);
    }

    @Override
    public StringBinding getStringBinding(ReadOnlyObjectProperty<?> key, Object... args) {
        return getBundle().getStringBinding(key, args);
    }

    @Override
    public Map<String, String> getStrings(Set<String> keys) {
        return keys.stream().collect(Collectors.toMap(k -> k, k -> getString(k)));
    }

    @Override
    public String getCss(String name) {
        String css = null;
        URL url = caller.getResource(CSS_DIR_NAME + "/" + name + ".css");

        if (url == null) {
            if (parent != null) {
                css = parent.getCss(name);
            }
        } else {
            css = url.toExternalForm();
        }
        return css;
    }

    public String getCss() {
        return getCss(CSS_FILE_NAME);
    }

    public List<String> getCss(String[] names) {
        return Arrays.stream(names).map(n -> getCss(n)).filter(s -> s != null).collect(Collectors.toList());
    }

    public Region getMediumIcon(String style) {

        return getIcon(MEDIUM_ICON_NAME, iconAltText, MEDIUM_ICON_SIZE, style);
    }

    public Region getSmallIcon(String style) {

        return getIcon(SMALL_ICON_NAME, iconAltText, SMALL_ICON_SIZE, style);
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
            String imgPath = ICON_DIR_NAME + "/" + name + ".png";
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

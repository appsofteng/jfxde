package dev.jfxde.logic;

import static javafx.scene.layout.Region.USE_PREF_SIZE;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.stream.Collectors;

import dev.jfxde.api.ResourceController;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Node;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class ResourceManager extends Manager implements ResourceController {

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
    }

    public static Locale getLocale() {
        return FXResourceBundle.getLocale();
    }

    public static void setLocale(String locale) {
        FXResourceBundle.setLocale(locale);
    }

    private FXResourceBundle getBundle() {
        FXResourceBundle resourceBundle = null;
        FXResourceBundle parentBundle = null;

        if (parent != null) {
            parentBundle = parent.getBundle();
        }

        if (parentBundle == null) {
            resourceBundle = FXResourceBundle.getBundle​(caller);
        } else {
           resourceBundle = FXResourceBundle.getBundle​(caller, parentBundle);
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
        return getBundle().getStringMaxWidth(key, arg, maxWidth);
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

    public Node getMediumIcon(String style) {

        return getIcon(MEDIUM_ICON_NAME, iconAltText, MEDIUM_ICON_SIZE, style);
    }

    public Node getSmallIcon(String style) {

        return getIcon(SMALL_ICON_NAME, iconAltText, SMALL_ICON_SIZE, style);
    }

    private Node getIcon(String name, String iconAltText, double size, String style) {
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

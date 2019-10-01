package dev.jfxde.jfxext.util.prefs;

import java.nio.file.Paths;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class FilePreferencesFactory implements PreferencesFactory {

    private static final String DEFAULT_SYSTEM_PREFERENCES = System.getProperty("dev.jfxde.jfxext.util.prefs.defaultSystemRoot");
    private static final String SYSTEM_PREFERENCES = System.getProperty("dev.jfxde.jfxext.util.prefs.systemRoot");
    private static final String DEFAULT_USER_PREFERENCES = System.getProperty("dev.jfxde.jfxext.util.prefs.defaultUserRoot");
    private static final String USER_PREFERENCES = System.getProperty("dev.jfxde.jfxext.util.prefs.userRoot");
    private Preferences systemRoot;
    private Preferences userRoot;

    @Override
    public Preferences systemRoot() {

        if (systemRoot == null) {
            systemRoot = new FilePreferences(null, "", Paths.get(DEFAULT_SYSTEM_PREFERENCES), Paths.get(SYSTEM_PREFERENCES));
        }

        return systemRoot;
    }

    @Override
    public Preferences userRoot() {
        if (userRoot == null) {
            userRoot = new FilePreferences(null, "", Paths.get(DEFAULT_USER_PREFERENCES), Paths.get(USER_PREFERENCES));
        }

        return userRoot;
    }
}

package dev.jfxde.j.util.prefs;

import java.nio.file.Path;
import java.util.prefs.Preferences;
import java.util.prefs.PreferencesFactory;

public class FilePreferencesFactory implements PreferencesFactory {

    private static final String DEFAULT_SYSTEM_PREFERENCES = System.getProperty("dev.jfxde.j.util.prefs.defaultSystemRoot");
    private static final String SYSTEM_PREFERENCES = System.getProperty("dev.jfxde.j.util.prefs.systemRoot");
    private static final String DEFAULT_USER_PREFERENCES = System.getProperty("dev.jfxde.j.util.prefs.defaultUserRoot");
    private static final String USER_PREFERENCES = System.getProperty("dev.jfxde.j.util.prefs.userRoot");
    private Preferences systemRoot;
    private Preferences userRoot;

    @Override
    public Preferences systemRoot() {

        if (systemRoot == null) {
            systemRoot = new FilePreferences(null, "", Path.of(DEFAULT_SYSTEM_PREFERENCES), Path.of(SYSTEM_PREFERENCES));
        }

        return systemRoot;
    }

    @Override
    public Preferences userRoot() {
        if (userRoot == null) {
            userRoot = new FilePreferences(null, "", Path.of(DEFAULT_USER_PREFERENCES), Path.of(USER_PREFERENCES));
        }

        return userRoot;
    }
}

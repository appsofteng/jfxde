package dev.jfxde.jfxext.util.prefs;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;

public class FilePreferences extends AbstractPreferences {

    private Map<String, String> values;
    private Map<String, FilePreferences> children;
    private boolean removed = false;
    private Path defaultPrefsFile;
    private Path prefsFile;

    protected FilePreferences(AbstractPreferences parent, String name, Path defaultPrefsFile, Path prefsFile) {
        super(parent, name);
        this.defaultPrefsFile = defaultPrefsFile;
        this.prefsFile = prefsFile;

        values = new TreeMap<String, String>();
        children = new TreeMap<String, FilePreferences>();

        try {
            sync();
        } catch (BackingStoreException e) {
            new RuntimeException(e);
        }
    }

    @Override
    protected void putSpi(String key, String value) {
        values.put(key, value);
        try {
            flush();
        } catch (BackingStoreException e) {
            new RuntimeException(e);
        }
    }

    @Override
    protected String getSpi(String key) {

        return values.get(key);
    }

    @Override
    protected void removeSpi(String key) {
        values.remove(key);
        try {
            flush();
        } catch (BackingStoreException e) {
            new RuntimeException(e);
        }
    }

    @Override
    protected void removeNodeSpi() throws BackingStoreException {
        removed = true;
        flush();
    }

    @Override
    protected String[] keysSpi() throws BackingStoreException {
        return values.keySet().toArray(new String[values.keySet().size()]);
    }

    @Override
    protected String[] childrenNamesSpi() throws BackingStoreException {
        return children.keySet().toArray(new String[children.keySet().size()]);
    }

    @Override
    protected AbstractPreferences childSpi(String name) {
        FilePreferences child = children.get(name);
        if (child == null || child.isRemoved()) {
            child = new FilePreferences(this, name, defaultPrefsFile, prefsFile);
            children.put(name, child);
        }
        return child;
    }

    @Override
    protected void syncSpi() throws BackingStoreException {
        if (isRemoved()) {
            return;
        }
        if (!Files.exists(defaultPrefsFile) && !Files.exists(prefsFile)) {
            return;
        }

        synchronized (prefsFile) {

            Properties defaultPreferences = new Properties();
            Properties preferences = new Properties(defaultPreferences);

            if (Files.exists(defaultPrefsFile)) {

                try (BufferedReader dr = Files.newBufferedReader(defaultPrefsFile)) {
                    defaultPreferences.load(dr);
                } catch (IOException e) {
                    throw new BackingStoreException(e);
                }
            }

            if (Files.exists(prefsFile)) {
                try (BufferedReader ur = Files.newBufferedReader(prefsFile)) {
                    preferences.load(ur);
                } catch (IOException e) {
                    throw new BackingStoreException(e);
                }
            }

            StringBuilder sb = new StringBuilder();
            getPath(sb);
            String path = sb.toString();

            for (String propKey : preferences.stringPropertyNames()) {

                String subKey = getSubKey(path, propKey);
                if (subKey != null) {
                    String childName = getChildName(subKey);
                    if (childName == null) {
                        values.put(subKey, preferences.getProperty(propKey));
                    } else {
                        children.put(childName, null);
                    }
                }
            }
        }
    }

    @Override
    protected void flushSpi() throws BackingStoreException {
        synchronized (prefsFile) {

            Properties preferences = new Properties();
            StringBuilder sb = new StringBuilder();
            getPath(sb);
            String path = sb.toString();
            removeSubproperties(preferences, path);

            if (!removed) {
                values.keySet().forEach(k -> preferences.setProperty(path + k, values.get(k)));
            }

            try (BufferedWriter bw = Files.newBufferedWriter(prefsFile)) {
                preferences.store(bw, "");
            } catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    private void removeSubproperties(Properties preferences, String path) throws BackingStoreException {

        if (Files.exists(prefsFile)) {
            try (BufferedReader ur = Files.newBufferedReader(prefsFile)) {
                preferences.load(ur);
                List<String> toRemove = new ArrayList<>();

                for (String propKey : preferences.stringPropertyNames()) {

                    String subKey = getSubKey(path, propKey);
                    if (subKey != null) {
                        if (subKey.indexOf('.') == -1) {

                            toRemove.add(propKey);
                        }
                    }
                }

                toRemove.forEach(k -> preferences.remove(k));

            } catch (IOException e) {
                throw new BackingStoreException(e);
            }
        }
    }

    private void getPath(StringBuilder sb) {
        final FilePreferences parent = (FilePreferences) parent();
        if (parent == null) {
            return;
        }

        parent.getPath(sb);
        sb.append(name()).append('.');
    }

    private String getSubKey(String path, String propKey) {
        String subKey = null;
        if (propKey.startsWith(path)) {
            subKey = propKey.substring(path.length());
        }

        return subKey;
    }

    private String getChildName(String subKey) {
        String childName = null;

        int i = subKey.indexOf('.');

        if (i > 0) {
            childName = subKey.substring(0, i);
        }

        return childName;
    }
}

package dev.jfxde.sysapps.jshell;

import java.util.List;

public class Settings {

    private List<String> loadFiles = List.of();
    private boolean loadDefault;
    private boolean loadPrinting;

    public boolean isLoadDefault() {
        return loadDefault;
    }

    public boolean isLoadPrinting() {
        return loadPrinting;
    }

    public List<String> getLoadFiles() {
        return loadFiles;
    }
}

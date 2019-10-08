package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private boolean loadDefault;
    private boolean loadPrinting;
    private List<String> loadFiles = new ArrayList<>();

    public boolean isLoadDefault() {
        return loadDefault;
    }

    public void setLoadDefault(boolean loadDefault) {
        this.loadDefault = loadDefault;
    }

    public void setLoadPrinting(boolean loadPrinting) {
        this.loadPrinting = loadPrinting;
    }

    public boolean isLoadPrinting() {
        return loadPrinting;
    }

    public List<String> getLoadFiles() {
        return loadFiles;
    }
}

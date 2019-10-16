package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;

public class Settings {

    private boolean loadDefault;
    private boolean loadPrinting;
    private boolean loadScripts;
    private List<String> startupScripts = new ArrayList<>();

    public boolean isLoadDefault() {
        return loadDefault;
    }

    public void setLoadDefault(boolean loadDefault) {
        this.loadDefault = loadDefault;
    }

    public boolean isLoadPrinting() {
        return loadPrinting;
    }

    public void setLoadPrinting(boolean loadPrinting) {
        this.loadPrinting = loadPrinting;
    }

    public boolean isLoadScripts() {
        return loadScripts;
    }

    public void setLoadScripts(boolean loadScripts) {
        this.loadScripts = loadScripts;
    }

    public List<String> getStartupScripts() {
        return startupScripts;
    }

    public void setStartupScripts(List<String> startupScripts) {
        this.startupScripts = startupScripts;
    }
}

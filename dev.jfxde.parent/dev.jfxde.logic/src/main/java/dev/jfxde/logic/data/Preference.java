package dev.jfxde.logic.data;

import java.util.prefs.Preferences;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Preference implements Comparable<Preference>{

    private StringProperty key;
    private StringProperty value;
    private Preferences preferences;
    private boolean leaf;

    public Preference() {

    }

    public Preference(String key, boolean leaf) {
        setKey(key);
        this.leaf = leaf;
    }

    public Preference(Preferences preferences, String key, boolean leaf) {
        this.preferences = preferences;
        setKey(key);
        setValue(preferences.get(key, null));

        this.leaf = leaf;

        value.addListener((v,o,n) -> {
            preferences.put(key, n);
        });
    }

    public Preferences getPreferences() {
        return preferences;
    }

    public String getPath() {
        return preferences.absolutePath() + "/" + getKey();
    }

    public String getKey() {
        return key == null ? null : key.get();
    }

    public final void setKey(String value) {
        keyProperty().set(value);
    }

    public final StringProperty keyProperty() {
        if (key == null) {
            key = new SimpleStringProperty();
        }
        return key;
    }

    public String getValue() {
        return preferences.get(getKey(), null);
    }

    public void setValue(String value) {
        valueProperty().set(value);
    }

    public final StringProperty valueProperty() {
        if (value == null) {
            value = new SimpleStringProperty();
        }
        return value;
    }

    public boolean isLeaf() {
        return leaf;
    }

    @Override
    public String toString() {
        return getKey();
    }

    @Override
    public int compareTo(Preference o) {
        return toString().compareTo(o.toString());
    }
}

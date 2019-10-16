package dev.jfxde.logic.data;

import java.util.Map;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PropertyDescriptor implements Comparable<PropertyDescriptor> {

    private StringProperty key = new SimpleStringProperty();
    private StringProperty value = new SimpleStringProperty();
    private StringProperty label = new SimpleStringProperty();

    public PropertyDescriptor() {
    }

    public PropertyDescriptor(Map.Entry<Object, Object> entry) {
        key.set(entry.getKey().toString());
        value.set(entry.getValue().toString());
    }

    public PropertyDescriptor(String key) {
        this(key, null);
    }

    public PropertyDescriptor(String key, String value) {
        this.key.set(key);
        this.value.set(value);
        this.label.set(getLabel());
    }

    public ReadOnlyStringProperty keyProperty() {
        return key;
    }

    public String getKey() {
        return key.get();
    }

    public ReadOnlyStringProperty valueProperty() {
        return value;
    }

    public String getValue() {
        return value.get();
    }

    public ReadOnlyStringProperty labelProperty() {
        return label;
    }


    @Override
    public int compareTo(PropertyDescriptor o) {
        return key.get().compareTo(o.key.get());
    }

    public boolean isLeaf() {
        return getValue() != null;
    }

    public boolean isSubproperty(PropertyDescriptor propertyDescriptor) {

        boolean result = false;

        if (propertyDescriptor.getKey() == null || propertyDescriptor.getKey().isEmpty()) {
            result = getLevel() == 1;
        } else {
            result = getKey().startsWith(propertyDescriptor.getKey()) && getLevel() - propertyDescriptor.getLevel() == 1;
        }

        return result;
    }

    private int getLevel() {

        int level = 0;

        if (getKey() != null && !getKey().isEmpty()) {
            level = getKey().split("\\.").length;
        }

        return level;
    }

    private String getLabel() {
        String label = "";

        if (getKey() != null && !getKey().isEmpty()) {
            String[] parts = getKey().split("\\.");
            label = parts[parts.length - 1];
        }

        return label;
    }
}

package dev.jfxde.logic.data;

import java.security.Permission;

import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class PermissionDescriptor {
    
    private final StringProperty type = new SimpleStringProperty();
    private final StringProperty target = new SimpleStringProperty();
    private final StringProperty actions = new SimpleStringProperty();
    
    public PermissionDescriptor(Permission permission) {
        type.set(permission.getClass().getName());
        target.set(permission.getName());
        actions.set(permission.getActions());
    }

    public ReadOnlyStringProperty typeProperty() {
        return type;
    }
    
    public ReadOnlyStringProperty targetProperty() {
        return target;
    }
    
    public ReadOnlyStringProperty actionsProperty() {
        return actions;
    }
}

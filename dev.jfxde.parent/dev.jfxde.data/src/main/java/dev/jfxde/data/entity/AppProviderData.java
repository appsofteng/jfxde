package dev.jfxde.data.entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class AppProviderData extends DataObj {

    private String _fqn;
    private boolean _allowed;
    private String _permissionChecksum = "";

    private transient StringProperty name;
    private transient BooleanProperty allowed;
    private transient StringProperty fqn;

    public AppProviderData() {
    }

    public AppProviderData(String name, String fqn) {
        this.setName(name);
        this.setFqn(fqn);
    }

    public String getName() {
        return name == null ? null : name.get();
    }

    public void setName(String value) {
        nameProperty().set(value);
    }

    public StringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty();
        }
        return name;
    }

    public String getFqn() {
        return _fqn;
    }

    public void setFqn(String value) {
        fqnProperty().set(value);
    }

    public StringProperty fqnProperty() {
        if (fqn == null) {
            fqn = new SimpleStringProperty(_fqn) {
                @Override
                public void set(String newValue) {
                    _fqn = newValue;
                    super.set(newValue);
                }
            };
        }

        return fqn;
    }

    public boolean isAllowed() {
        return _allowed;
    }

    public void setAllowed(boolean value) {
        allowedProperty().set(value);
    }

    public BooleanProperty allowedProperty() {
        if (allowed == null) {
            allowed = new SimpleBooleanProperty(_allowed) {
                @Override
                public void set(boolean newValue) {
                    _allowed = newValue;
                    super.set(newValue);
                }
            };
        }

        return allowed;
    }

    public String getPermissionChecksum() {

        if (_permissionChecksum == null) {
            _permissionChecksum = "";
        }

        return _permissionChecksum;
    }

    public void setPermissionChecksum(String value) {
        this._permissionChecksum = value;
    }

    @Override
    public AppProviderData copy() {
        var copy = new AppProviderData();

        copy._fqn = _fqn;
        copy._allowed = _allowed;
        copy._permissionChecksum = _permissionChecksum;

        return copy;
    }
}

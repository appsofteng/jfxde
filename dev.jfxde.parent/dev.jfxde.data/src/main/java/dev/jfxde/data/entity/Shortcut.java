package dev.jfxde.data.entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Shortcut extends DataObj {

    private String _name;
    private String _fqn;
    private String _uri;
    private int _position;

    private transient StringProperty name;
    private transient IntegerProperty position;
    private transient BooleanProperty active;
    private Desktop desktop;

    public Shortcut() {
    }

    public Shortcut(AppProviderData appProviderData) {
        this.setName(appProviderData.getName());
        this._fqn = appProviderData.getFqn();
    }

    public Shortcut(String name, String fqn, String uri) {
        this.setName(name);
        this._fqn = fqn;
        this._uri = uri;
    }

    public String getName() {
        return _name;
    }

    public void setName(String value) {
        nameProperty().set(_name = value);
    }

    public StringProperty nameProperty() {
        if (name == null) {
            name = new SimpleStringProperty(_name);
        }
        return name;
    }

    public int getPosition() {
        return _position;
    }

    public void setPosition(int value) {
        positionProperty().set(_position = value);
    }

    public IntegerProperty positionProperty() {

        if (position == null) {
            position = new SimpleIntegerProperty(_position);
        }

        return position;
    }

    public String getFqn() {
        return _fqn;
    }

    public void setFqn(String fqn) {
        this._fqn = fqn;
    }

    public String getUri() {
        return _uri;
    }

    public Desktop getDesktop() {
        return desktop;
    }

    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
    }

    public BooleanProperty activeProperty() {

        if (active == null) {
            active = new SimpleBooleanProperty();
        }

        return active;
    }

    public boolean isActive() {
        return active == null ? false : active.get();
    }

    void setActive(boolean value) {
        activeProperty().set(value);
    }

    public void activate() {
        desktop.setActiveShortcut(this);
    }

    public void moved(int position) {
        setPosition(position);
        desktop.moveShortcut(this);
    }

    public void remove() {
        desktop.removeShortcut(this);
    }

    @Override
    public Shortcut copy() {
        var copy = new Shortcut();
        copy._name = _name;
        copy._fqn = _fqn;
        copy._uri = _uri;
        copy._position = _position;

        return copy;
    }
}

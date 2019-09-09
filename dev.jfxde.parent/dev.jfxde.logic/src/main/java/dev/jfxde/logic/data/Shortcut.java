package dev.jfxde.logic.data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyIntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class Shortcut extends DataObj {

    private StringProperty name = new SimpleStringProperty();
    private String fqn;
    private String uri;
    private IntegerProperty position = new SimpleIntegerProperty();
    private BooleanProperty active = new SimpleBooleanProperty();
    private Desktop desktop;

    public Shortcut() {
	}

    public Shortcut(AppProviderDescriptor descriptor) {
        this.name.set(descriptor.getName());
        this.fqn = descriptor.getAppManifest().fqn();
    }

    public Shortcut(String name, String fqn, String uri) {
        this.name.set(name);
        this.fqn = fqn;
        this.uri = uri;
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String value) {
        name.set(value);
    }

    public ReadOnlyIntegerProperty positionProperty() {
        return position;
    }

    public int getPosition() {
        return position.get();
    }

    public void setPosition(int value) {
        position.set(value);
    }

    public String getFqn() {
        return fqn;
    }

    public void setFqn(String fqn) {
		this.fqn = fqn;
	}

    public String getUri() {
        return uri;
    }

    public Desktop getDesktop() {
        return desktop;
    }

    public void setDesktop(Desktop desktop) {
        this.desktop = desktop;
    }

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    public boolean isActive() {
        return active.get();
    }

    void setActive(boolean value) {
        active.set(value);
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
}

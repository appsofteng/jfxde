package dev.jfxde.data.entity;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;

public class Window {

    private Desktop desktop;
    private Object appDescriptor;
    private final BooleanProperty active = new SimpleBooleanProperty();
    private final ObjectProperty<State> state = new SimpleObjectProperty<>(State.RESTORED);
    private final ObjectProperty<State> previousState = new SimpleObjectProperty<>(State.RESTORED);

    public Window(Object appDescriptor) {
    	this.appDescriptor = appDescriptor;
    	state.addListener((v, o, n) -> previousState.set(o));
	}

    public void reset() {
        state.set(State.RESTORED);
        previousState.set(State.RESTORED);
    }

	public Desktop getDesktop() {
		return desktop;
	}

	public void setDesktop(Desktop desktop) {
		this.desktop = desktop;
	}

	public <T> T getAppDescriptor() {
		return (T) appDescriptor;
	}

    public ReadOnlyBooleanProperty activeProperty() {
        return active;
    }

    public boolean isActive() {
        return active.get();
    }

    public void setActive(boolean value) {
        active.set(value);
    }

    public ReadOnlyObjectProperty<State> stateProperty() {
        return state;
    }

    public State getState() {
        return state.get();
    }

    public void setState(State value) {
        state.set(value);
    }

    public boolean isMinimized() {
        return state.get() == State.MINIMIZED;
    }

    public boolean isMaximized() {
        return state.get() == State.MAXIMIZED;
    }

    public boolean isFull() {
        return state.get() == State.FULL;
    }

    public boolean isTiled() {
        return state.get() == State.TILED;
    }

    public boolean isRestored() {
        return state.get() == State.RESTORED;
    }

    public State getPreviousState() {
        return previousState.get();
    }

    public void minimize() {
        state.set(State.MINIMIZED);
    }

    public void maximize() {
        state.set(State.MAXIMIZED);
    }

    public void full() {
        state.set(State.FULL);
    }

    public void tile() {
        state.set(State.TILED);
    }

    public void close() {
        state.set(State.CLOSED);
    }

    public void restore() {
        state.set(State.RESTORED);
    }

    public void minimizeActivate() {
        state.set(State.MINIMIZED);
        desktop.activateWindow();
    }

    public void minimizeUnminimize() {
        if (isMinimized()) {
            activate();
            toPreviousState();
            resetPreviousState();
        } else if (!isActive()) {
            activate();
        }  else {
            minimizeActivate();
        }
    }

    public void activateUnminimize() {
        activate();
        if (isMinimized()) {
            toPreviousState();
        }
    }

    public void reminimize() {
        if (getPreviousState() == State.MINIMIZED) {
            state.set(getPreviousState());
        }
    }

    public void maximizeRestore() {
        if (isRestored() || isTiled()) {
            maximize();
        } else {
            restore();
        }
    }

    public void fullRestore() {

        if (isFull()) {
            toPreviousState();
        } else {
            full();
        }
    }

    public void resetPreviousState() {
        previousState.set(state.get());
    }

    private void toPreviousState() {
        state.set(getPreviousState());
    }

    public void activate() {
        desktop.setActiveWindow(this);
    }

    public void remove() {
    	desktop.removeWindow(this);
    }

    public enum State {
        MINIMIZED, MAXIMIZED, FULL, RESTORED, TILED, CLOSED
    }
}

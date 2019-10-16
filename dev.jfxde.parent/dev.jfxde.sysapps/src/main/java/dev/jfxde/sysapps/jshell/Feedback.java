package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;

public class Feedback {

    private Mode mode = Mode.NORMAL;
    private ConsoleModel consoleModel;
    private boolean cached;
    private List<TextStyleSpans> cache = new ArrayList<>();

    public Feedback(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
    }

    public void setCached(boolean cached) {
        this.cached = cached;
    }

    public void flush() {
        cached = false;
        cache.forEach(s -> normal(s));
        cache.clear();
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void normal(String message) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            normal(new TextStyleSpans(message));
        }
    }

    public void normaln(String message) {
        normal(message + "\n");
    }

    public void normal(String message, String style) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            normal(new TextStyleSpans(message, style));
        }
    }

    public void normaln(String message, String style) {
        normal(message + "\n", style);
    }

    public void normal(TextStyleSpans span) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            if (cached) {
                cache.add(span);
            } else {
                consoleModel.addNewLineOutput(span);
            }
        }
    }

    public enum Mode {
        SILENT, NORMAL
    }

}

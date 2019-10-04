package dev.jfxde.sysapps.jshell;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;

public class Feedback {

    private Mode mode = Mode.NORMAL;
    private ConsoleModel consoleModel;

    public Feedback(ConsoleModel consoleModel) {
        this.consoleModel = consoleModel;
    }

    public void setMode(Mode mode) {
        this.mode = mode;
    }

    public void normal(String message) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            consoleModel.addNewLineOutput(new TextStyleSpans(message));
        }
    }

    public void normaln(String message) {
        normal(message + "\n");
    }

    public void normal(String message, String style) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            consoleModel.addNewLineOutput(new TextStyleSpans(message, style));
        }
    }

    public void normaln(String message, String style) {
        normal(message + "\n", style);
    }

    public void normal(TextStyleSpans span) {
        if (mode.ordinal() >= Mode.NORMAL.ordinal()) {
            consoleModel.addNewLineOutput(span);
        }
    }

    public enum Mode {
        SILENT, NORMAL
    }

}

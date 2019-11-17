package dev.jfxde.sysapps.editor;

import dev.jfxde.logic.data.FXPath;

public class LinePointer extends FilePointer {

    private PathPointer pathPointer;
    private int line;
    private int column;
    private String text = "";

    public LinePointer(int line, int column, String text) {
        this.line = line;
        this.column = column;
        this.text = text;
    }

    public void setPathPointer(PathPointer pathPointer) {
        this.pathPointer = pathPointer;
    }

    @Override
    public FXPath getPath() {
        return pathPointer.getPath();
    }

    @Override
    public String toString() {
        return line + "," + column + ": " + text;
    }
}

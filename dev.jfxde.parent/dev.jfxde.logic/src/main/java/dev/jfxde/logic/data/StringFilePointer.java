package dev.jfxde.logic.data;

import java.util.List;

public class StringFilePointer extends FilePointer {

    private PathFilePointer pathPointer;
    private int line;
    private int column;
    private String text = "";

    public StringFilePointer(int line, int column, String text) {
        this.line = line;
        this.column = column;
        this.text = text;
    }

    public void setPathFilePointer(PathFilePointer pathPointer) {
        this.pathPointer = pathPointer;
    }

    @Override
    public List<StringFilePointer> getStringFilePointers() {
        return pathPointer.getStringFilePointers();
    }

    @Override
    public FXPath getPath() {
        return pathPointer.getPath();
    }

    @Override
    public StringFilePointer current() {
        return this;
    }

    @Override
    public String toString() {
        return line + "," + column + ": " + text;
    }
}

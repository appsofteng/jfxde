package dev.jfxde.sysapps.editor;

import dev.jfxde.logic.data.FXPath;

public class PathPointer extends FilePointer {

    private FXPath path;

    public PathPointer(FXPath path) {
        this.path = path;
    }

    public FXPath getPath() {
        return path;
    }

    public void add(LinePointer linePointer) {
        linePointer.setPathPointer(this);
        getFilePointers().add(linePointer);
    }

    @Override
    public String toString() {
        return getPath().getPath().toString();
    }
}

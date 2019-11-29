package dev.jfxde.logic.data;

import java.util.List;

import dev.jfxde.j.util.search.StringRef;

public class StringFilePosition extends FilePosition {

    private PathFilePosition pathPointer;
    private StringRef stringRef;

    public StringFilePosition(StringRef stringRef) {
        this.stringRef = stringRef;
    }

    public void setPathFilePointer(PathFilePosition pathPointer) {
        this.pathPointer = pathPointer;
    }

    public StringRef getStringRef() {
        return stringRef;
    }

    @Override
    public List<StringFilePosition> getStringFilePositions() {
        return pathPointer.getStringFilePositions();
    }

    @Override
    public FXPath getPath() {
        return pathPointer.getPath();
    }

    @Override
    public StringFilePosition getSelectedPosition() {
        return this;
    }

    @Override
    public String toString() {
        return stringRef.toString();
    }
}

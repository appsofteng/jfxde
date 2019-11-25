package dev.jfxde.logic.data;

import java.util.List;

import dev.jfxde.fxmisc.richtext.StringRef;

public class StringFilePointer extends FilePointer {

    private PathFilePointer pathPointer;
    private StringRef stringRef;

    public StringFilePointer(StringRef stringRef) {
        this.stringRef = stringRef;
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
        return stringRef.toString();
    }
}

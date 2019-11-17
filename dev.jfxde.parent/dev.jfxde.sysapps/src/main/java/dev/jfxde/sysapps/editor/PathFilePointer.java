package dev.jfxde.sysapps.editor;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.logic.data.FXPath;

public class PathFilePointer extends FilePointer {

    private FXPath path;
    private List<StringFilePointer> stringFilePointers = new ArrayList<>();

    public PathFilePointer(FXPath path) {
        this.path = path;
    }

    public FXPath getPath() {
        return path;
    }

    @Override
    public List<StringFilePointer> getStringFilePointers() {
        return stringFilePointers;
    }

    @Override
    public StringFilePointer current() {
        StringFilePointer pointer = getStringFilePointers().isEmpty() ? null :  getStringFilePointers().get(0);
        return pointer;
    }

    public void add(StringFilePointer stringFilePointer) {
        stringFilePointer.setPathFilePointer(this);
        getStringFilePointers().add(stringFilePointer);
    }

    @Override
    public String toString() {
        return getPath().getPath().toString();
    }
}

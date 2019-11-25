package dev.jfxde.logic.data;

import java.util.ArrayList;
import java.util.List;

public class PathFilePosition extends FilePosition {

    private FXPath path;
    private List<StringFilePosition> stringFilePointers = new ArrayList<>();

    public PathFilePosition(FXPath path) {
        this.path = path;
    }

    public FXPath getPath() {
        return path;
    }

    @Override
    public List<StringFilePosition> getStringFilePositions() {
        return stringFilePointers;
    }

    @Override
    public StringFilePosition getSelectedPosition() {
        StringFilePosition position = getStringFilePositions().isEmpty() ? null :  getStringFilePositions().get(0);
        return position;
    }

    public void add(StringFilePosition stringFilePosition) {
        stringFilePosition.setPathFilePointer(this);
        getStringFilePositions().add(stringFilePosition);
    }

    @Override
    public String toString() {
        return getPath().getPath().toString();
    }
}

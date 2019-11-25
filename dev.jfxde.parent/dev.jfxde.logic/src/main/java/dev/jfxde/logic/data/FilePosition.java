package dev.jfxde.logic.data;

import java.util.List;

public abstract class FilePosition {

    public abstract FXPath getPath();

    public abstract StringFilePosition getSelectedPosition();

    public abstract List<StringFilePosition> getStringFilePositions();
}

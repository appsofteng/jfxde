package dev.jfxde.logic.data;

import java.util.List;

public abstract class FilePointer {

    public abstract FXPath getPath();

    public abstract StringFilePointer current();

    public abstract List<StringFilePointer> getStringFilePointers();
}

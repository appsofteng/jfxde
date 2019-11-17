package dev.jfxde.sysapps.editor;

import java.util.List;

import dev.jfxde.logic.data.FXPath;

public abstract class FilePointer {

    public abstract FXPath getPath();

    public abstract StringFilePointer current();

    public abstract List<StringFilePointer> getStringFilePointers();
}

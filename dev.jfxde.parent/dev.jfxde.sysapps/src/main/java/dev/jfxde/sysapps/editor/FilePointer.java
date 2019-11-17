package dev.jfxde.sysapps.editor;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.logic.data.FXPath;

public abstract class FilePointer {

    private List<FilePointer> filePointers = new ArrayList<>();

    public abstract FXPath getPath();

    public List<FilePointer> getFilePointers() {
        return filePointers;
    }
}

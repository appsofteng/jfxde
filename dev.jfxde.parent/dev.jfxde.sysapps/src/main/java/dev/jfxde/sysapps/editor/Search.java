package dev.jfxde.sysapps.editor;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePointer;

public class Search {

    private List<FXPath> paths = List.of();
    private String pathPattern = "";
    private String textPattern = "";
    private List<FilePointer> result = new ArrayList<>();

    public Search(List<FXPath> paths) {
        this.paths = paths;
    }

    public List<FXPath> getPaths() {
        return paths;
    }

    public String getPathPattern() {
        return pathPattern;
    }

    public void setPathPattern(String pathPattern) {
        this.pathPattern = pathPattern;
    }

    public String getTextPattern() {
        return textPattern;
    }

    public void setTextPattern(String textPattern) {
        this.textPattern = textPattern;
    }

    public List<FilePointer> getResult() {
        return result;
    }

    @Override
    public String toString() {
        return paths.toString();
    }
}

package dev.jfxde.sysapps.editor;

import java.util.ArrayList;
import java.util.List;

import dev.jfxde.logic.data.FXPath;
import dev.jfxde.logic.data.FilePosition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class Search {

    private ObservableList<FXPath> paths = FXCollections.observableArrayList();
    private String pathPattern = "";
    private String textPattern = "";
    private ObservableList<FilePosition> result = FXCollections.observableArrayList();

    public Search(ObservableList<FXPath> paths) {
        this.paths = paths;
    }

    public ObservableList<FXPath> getPaths() {
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

    public ObservableList<FilePosition> getResult() {
        return result;
    }

    public boolean remove(FXPath path) {
        boolean removed = paths.removeIf(p -> p.equals(path));
        removed |= result.removeIf(p -> p.getPath().equals(path));

        return removed;
    }

    @Override
    public String toString() {
        return paths.toString();
    }
}

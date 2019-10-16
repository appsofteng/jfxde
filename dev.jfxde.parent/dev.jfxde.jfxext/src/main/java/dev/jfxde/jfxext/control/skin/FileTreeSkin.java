package dev.jfxde.jfxext.control.skin;

import java.nio.file.Path;

import dev.jfxde.jfxext.control.FileTree;
import javafx.scene.control.SkinBase;
import javafx.scene.control.TreeView;

public class FileTreeSkin extends SkinBase<FileTree> {

    private TreeView<Path> tree;;

    public FileTreeSkin(FileTree control) {
        super(control);
    }
}

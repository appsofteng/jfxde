package dev.jfxde.jfxext.control;

import dev.jfxde.jfxext.control.skin.FileTreeSkin;
import javafx.scene.control.Control;
import javafx.scene.control.Skin;

public class FileTree extends Control {


    @Override
    protected Skin<?> createDefaultSkin() {
        return new FileTreeSkin(this);
    }
}

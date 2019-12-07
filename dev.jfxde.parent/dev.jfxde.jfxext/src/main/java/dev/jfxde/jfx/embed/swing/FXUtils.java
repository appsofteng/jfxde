package dev.jfxde.jfx.embed.swing;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;

public final class FXUtils {

    private FXUtils() {

    }

   public static Image getIcon(Path path) {

       if (path == null) {
           return null;
       }

        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
        BufferedImage image = (BufferedImage) icon.getImage();
        Image fximage = SwingFXUtils.toFXImage(image, null);

        return fximage;
    }
}

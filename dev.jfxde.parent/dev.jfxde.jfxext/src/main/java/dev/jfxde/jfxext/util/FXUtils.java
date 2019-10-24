package dev.jfxde.jfxext.util;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class FXUtils {

    private FXUtils() {

    }

   public static ImageView getIcon(Path path) {
        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
        BufferedImage image = (BufferedImage) icon.getImage();
        Image fxIcon = SwingFXUtils.toFXImage(image, null);
        ImageView imageView = (new ImageView(fxIcon));

        return imageView;
    }
}

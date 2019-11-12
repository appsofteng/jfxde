package dev.jfxde.jfx.embed.swing;

import java.awt.image.BufferedImage;
import java.nio.file.Path;

import javax.swing.ImageIcon;
import javax.swing.filechooser.FileSystemView;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public final class FXUtils {

    private FXUtils() {

    }

   public static Label getIcon(Path path) {

       if (path == null) {
           return null;
       }

        ImageIcon icon = (ImageIcon) FileSystemView.getFileSystemView().getSystemIcon(path.toFile());
        BufferedImage image = (BufferedImage) icon.getImage();
        Image fxIcon = SwingFXUtils.toFXImage(image, null);
        ImageView imageView = (new ImageView(fxIcon));

        return new Label("", imageView);
    }
}

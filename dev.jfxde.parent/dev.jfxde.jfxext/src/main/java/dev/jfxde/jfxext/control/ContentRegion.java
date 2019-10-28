package dev.jfxde.jfxext.control;

import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;
import javafx.scene.shape.Rectangle;

public class ContentRegion extends Region {

    private Node content;
    private Rectangle clip = new Rectangle();

    public ContentRegion() {
        setClip(clip);
    }

    public void setContent(Node content) {
        this.content = content;
        getChildren().remove(content);
        getChildren().add(content);
    }

    public void removeContent() {
        content = null;
        getChildren().remove(content);
    }

    @Override
    protected void layoutChildren() {
        double width = getWidth();
        double height = getHeight();
        clip.setWidth(width);
        clip.setHeight(height);

        if (content != null) {
            layoutInArea(content, 0, 0, width, height, 0, new Insets(0), HPos.CENTER, VPos.CENTER);
        }
    }
}

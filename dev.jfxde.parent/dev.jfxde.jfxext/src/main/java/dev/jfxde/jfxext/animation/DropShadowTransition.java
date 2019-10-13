package dev.jfxde.jfxext.animation;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.animation.Transition;
import javafx.scene.CacheHint;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public class DropShadowTransition extends Transition {

    private DropShadow shadow;
    private Node node;
    private Timeline timeline;
    private boolean cached = false;
    private CacheHint cacheHint = CacheHint.DEFAULT;
    private final boolean useCache=true;

    public DropShadowTransition(DropShadow shadow, Node node) {
        this.shadow = shadow;
        this.node = node;
        setCycleDuration(Duration.seconds(2));
        setCycleCount(5);
        setRate(15);

        statusProperty().addListener((ov, t, newStatus) -> {
            switch(newStatus) {
                case RUNNING:
                    starting();
                    break;
                default:
                    stopping();
                    break;
            }
         });

        createTimeline();
    }

    private void createTimeline() {
        timeline = new Timeline();
        timeline.getKeyFrames().setAll(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(shadow.colorProperty(), Color.WHITE)),
                new KeyFrame(Duration.seconds(1),
                        new KeyValue(shadow.colorProperty(), shadow.getColor())));
    }

    @Override
    protected void interpolate(double frac) {
        timeline.playFrom(Duration.seconds(frac));
        timeline.stop();
    }

    private void starting() {
        if (useCache) {
            cached = node.isCache();
            cacheHint = node.getCacheHint();
            node.setCache(true);
            node.setCacheHint(CacheHint.SPEED);
        }
    }

    private void stopping() {
        if (useCache) {
            node.setCache(cached);
            node.setCacheHint(cacheHint);
        }
    }
}

package dev.jfxde.jfx.scene.layout;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.TilePane;
import javafx.util.Callback;

public final class LayoutUtils {

    private LayoutUtils() {
    }

    public static void tile(Pane pane, Consumer<Pane> pretile) {
        List<Node> visibleChildren = pane.getChildren().stream().filter(Node::isVisible).collect(Collectors.toList());

        int cols = pane.getChildren().size() > 1 ? 2 : 1;
        int rows = visibleChildren.size() / cols + (int) Math.signum(visibleChildren.size() % cols);
        double w = pane.getWidth() / cols;
        double h = pane.getHeight() / rows;

        IntStream.range(0, visibleChildren.size())
                .mapToObj(i -> new SimpleEntry<Integer, Node>(Integer.valueOf(i), visibleChildren.get(i)))
                .filter(e -> e.getValue().isVisible()).forEach(e -> {
                    Pane child = (Pane) e.getValue();
                    pretile.accept(child);
                    double x = e.getKey() % cols * w;
                    double y = e.getKey() / cols * h;
                    child.relocate(x, y);
                    Pane content = child.getChildren().size() == 1 ? (Pane) child.getChildren().get(0) : child;
                    content.prefWidthProperty().unbind();
                    content.prefHeightProperty().unbind();
                    content.setPrefSize(w, h);
                });
    }

    private static boolean isInScene(Scene scene, double sceneX, double sceneY) {
        boolean result = sceneX > 0 && sceneX < scene.getWidth() && sceneY > 0 && sceneY < scene.getHeight();
        return result;
    }

    private static boolean isUseComputedSize(Region region) {
        return region.getPrefWidth() == Region.USE_COMPUTED_SIZE || region.getPrefHeight() == Region.USE_COMPUTED_SIZE;
    }

    public static void makeDragable(Node node, Node childNodeToDrag, Callback<MouseEvent, Point2D> pressHandler,
            Runnable dragHandler) {
        final double[] dragDiff = new double[2];
        childNodeToDrag.setOnMousePressed(mouseEvent -> {
            Point2D point = pressHandler.call(mouseEvent);
            dragDiff[0] = point.getX() - mouseEvent.getScreenX();
            dragDiff[1] = point.getY() - mouseEvent.getScreenY();
        });
        childNodeToDrag.setOnMouseDragged(mouseEvent -> {
            dragHandler.run();
            if (node.getCursor() == Cursor.DEFAULT) {
                if (isInScene(node.getScene(), mouseEvent.getSceneX(), mouseEvent.getSceneY())) {
                    double x = mouseEvent.getScreenX() + dragDiff[0];
                    double y = mouseEvent.getScreenY() + dragDiff[1];

                    node.relocate(x, y);
                }

            }
        });
    }

    public static void makeResizable(Node resizableNode, Region preferredSizeNode, double cursorDetectionBorderWidth) {

        resizableNode.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            if (resizableNode.isResizable() && !isUseComputedSize(preferredSizeNode) && !preferredSizeNode.isDisabled()) {
                addResizeCursors(resizableNode, cursorDetectionBorderWidth, new Point2D(e.getX(), e.getY()));
            }
        });

        resizableNode.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {

            if (!resizableNode.isResizable() || isUseComputedSize(preferredSizeNode) || preferredSizeNode.isDisabled()
                    || !isInScene(resizableNode.getScene(), e.getSceneX(), e.getSceneY())) {
                return;
            }

            Point2D position = new Point2D(resizableNode.getLayoutX(), resizableNode.getLayoutY());
            Dimension2D preferredSize = new Dimension2D(preferredSizeNode.getPrefWidth(), preferredSizeNode.getPrefHeight());
            Dimension2D minSize = new Dimension2D(preferredSizeNode.getMinWidth(), preferredSizeNode.getMinHeight());
            Bounds bounds = resize(resizableNode, position, preferredSize, minSize, new Point2D(e.getX(), e.getY()));

            resizableNode.setLayoutX(bounds.getMinX());
            resizableNode.setLayoutY(bounds.getMinY());

            preferredSizeNode.setPrefSize(bounds.getWidth(), bounds.getHeight());
        });
    }

    public static void makeResizable(Tooltip window, Node resizableNode, double cursorDetectionBorderWidth) {
        resizableNode.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {
            Point2D deltaPosition = new Point2D(e.getX(), e.getY());
            addResizeCursors(resizableNode, cursorDetectionBorderWidth, deltaPosition);
        });

        window.addEventFilter(MouseEvent.MOUSE_DRAGGED, e -> {

            Point2D deltaPosition = new Point2D(e.getScreenX() - window.getX(), e.getScreenY() - window.getY());

            if (!resizableNode.isResizable() || deltaPosition.getX() == 0 || deltaPosition.getY() == 0) {
                return;
            }

            Point2D position = new Point2D(window.getX(), window.getY());
            Dimension2D preferredSize = new Dimension2D(window.getPrefWidth(), window.getPrefHeight());
            Dimension2D minSize = new Dimension2D(window.getMinWidth(), window.getMinHeight());

            Bounds bounds = resize(resizableNode, position, preferredSize, minSize, deltaPosition);

            window.setX(bounds.getMinX());
            window.setY(bounds.getMinY());
            window.setPrefSize(bounds.getWidth(), bounds.getHeight());

        });
    }

    private static void addResizeCursors(Node resizableNode, double cursorDetectionBorderWidth, Point2D e) {

        if (!resizableNode.isResizable()) {
            resizableNode.setCursor(Cursor.DEFAULT);
            return;
        }

        double mouseX = e.getX();
        double mouseY = e.getY();

        double width = resizableNode.getLayoutBounds().getWidth();
        double height = resizableNode.getLayoutBounds().getHeight();

        if (Math.abs(mouseX) < cursorDetectionBorderWidth && Math.abs(mouseY) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.NW_RESIZE);
        } else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth
                && Math.abs(mouseY - height) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.SE_RESIZE);
        } else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth
                && Math.abs(mouseY) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.NE_RESIZE);
        } else if (Math.abs(mouseX) < cursorDetectionBorderWidth
                && Math.abs(mouseY - height) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.SW_RESIZE);
        } else if (Math.abs(mouseY) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.N_RESIZE);
        } else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.E_RESIZE);
        } else if (Math.abs(mouseY - height) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.S_RESIZE);
        } else if (Math.abs(mouseX) < cursorDetectionBorderWidth) {
            resizableNode.setCursor(Cursor.W_RESIZE);
        } else {
            resizableNode.setCursor(Cursor.DEFAULT);
        }
    }

    private static Bounds resize(Node resizableNode, Point2D position, Dimension2D preferredSize, Dimension2D minSize, Point2D e) {
        // When mouse moved left the cursor coordinates are negative so
        // when they are subtracted from the size they are actually
        // added.

        double x = position.getX();
        double y = position.getY();
        double width = preferredSize.getWidth();
        double height = preferredSize.getHeight();

        if (resizableNode.getCursor() == Cursor.NW_RESIZE) {
            x = x + e.getX();
            y = y + e.getY();
            width = width - e.getX();
            height = height - e.getY();
        } else if (resizableNode.getCursor() == Cursor.SE_RESIZE) {
            width = e.getX();
            height = e.getY();
        } else if (resizableNode.getCursor() == Cursor.NE_RESIZE) {
            y = y + e.getY();
            width = e.getX();
            height = height - e.getY();
        } else if (resizableNode.getCursor() == Cursor.SW_RESIZE) {
            x = x + e.getX();
            width = width - e.getX();
            height = e.getY();
        } else if (resizableNode.getCursor() == Cursor.N_RESIZE) {
            y = y + e.getY();
            height = height - e.getY();
        } else if (resizableNode.getCursor() == Cursor.E_RESIZE) {
            width = e.getX();
        } else if (resizableNode.getCursor() == Cursor.S_RESIZE) {
            height = e.getY();
        } else if (resizableNode.getCursor() == Cursor.W_RESIZE) {
            x = x + e.getX();
            width = width - e.getX();
        }

        if (x != position.getX() && width < minSize.getWidth()) {
            x = position.getX() + preferredSize.getWidth() - minSize.getWidth();
        }

        if (y != position.getY() && height < minSize.getHeight()) {
            y = position.getY() + preferredSize.getHeight() - minSize.getHeight();
        }

        width = Math.max(width, minSize.getWidth());
        height = Math.max(height, minSize.getHeight());

        BoundingBox bounds = new BoundingBox(x, y, width, height);

        return bounds;
    }

    public static TilePane createTilePane(List<? extends Region> nodes) {
        return createTilePane(nodes.size(), 5, nodes);
    }

    public static TilePane createTilePane(int padding, List<? extends Region> nodes) {
        return createTilePane(nodes.size(), padding, nodes);
    }

    public static TilePane createTilePane(int prefCols, int padding, List<? extends Region> nodes) {
        nodes.forEach(b -> b.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE));
        TilePane pane = new TilePane();
        pane.setPrefColumns(prefCols);
        pane.setPadding(new Insets(padding));
        pane.setHgap(padding);
        pane.getChildren().addAll(nodes);

        return pane;
    }

}

package dev.jfxde.ui;

import java.util.AbstractMap.SimpleEntry;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javafx.geometry.Dimension2D;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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

	/*
	 * Keep the actual size because the layout may not exactly use the preferred
	 * size of the child root when the size must be changed together with the
	 * position of the node i.e. resize to the west.
	 */
	public static void makeResizable(Node node, Region nodeRootChild, double cursorDetectionBorderWidth) {
		node.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {

			if (!node.isResizable()) {
				return;
			}

			double mouseX = e.getX();
			double mouseY = e.getY();

			double width = node.boundsInLocalProperty().get().getWidth();
			double height = node.boundsInLocalProperty().get().getHeight();

			if (Math.abs(mouseX) < cursorDetectionBorderWidth && Math.abs(mouseY) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.SE_RESIZE);
			} else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth
					&& Math.abs(mouseY) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.SW_RESIZE);
			} else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth
					&& Math.abs(mouseY - height) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.NW_RESIZE);
			} else if (Math.abs(mouseX) < cursorDetectionBorderWidth
					&& Math.abs(mouseY - height) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.NE_RESIZE);
			} else if (Math.abs(mouseY) < cursorDetectionBorderWidth) {

				node.setCursor(Cursor.N_RESIZE);
			} else if (Math.abs(mouseX - width) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.E_RESIZE);
			} else if (Math.abs(mouseY - height) < cursorDetectionBorderWidth) {

				node.setCursor(Cursor.S_RESIZE);
			} else if (Math.abs(mouseX) < cursorDetectionBorderWidth) {
				node.setCursor(Cursor.W_RESIZE);
			} else {
				node.setCursor(Cursor.DEFAULT);
			}
		});

		node.setOnMouseDragged(e -> {

			if (!node.isResizable() || !isInScene(node.getScene(), e.getSceneX(), e.getSceneY())) {
				return;
			}

			// When mouse moved left the cursor coordinates are negative so
			// when they are subtracted from the size they are actually
			// added.
			Dimension2D actualRootSize = new Dimension2D(nodeRootChild.getPrefWidth(), nodeRootChild.getPrefHeight());
			Dimension2D updatedRootSize = actualRootSize;

			if (node.getCursor() == Cursor.SE_RESIZE) {
				node.setLayoutX(node.getLayoutX() + e.getX());
				node.setLayoutY(node.getLayoutY() + e.getY());
				updatedRootSize = new Dimension2D(actualRootSize.getWidth() - e.getX(),
						actualRootSize.getHeight() - e.getY());
			} else if (node.getCursor() == Cursor.SW_RESIZE) {
				node.setLayoutY(node.getLayoutY() + e.getY());
				updatedRootSize = new Dimension2D(e.getX(), actualRootSize.getHeight() - e.getY());
			} else if (node.getCursor() == Cursor.NW_RESIZE) {
				updatedRootSize = new Dimension2D(e.getX(), e.getY());
			} else if (node.getCursor() == Cursor.NE_RESIZE) {
				node.setLayoutX(node.getLayoutX() + e.getX());
				updatedRootSize = new Dimension2D(actualRootSize.getWidth() - e.getX(), e.getY());
			} else if (node.getCursor() == Cursor.N_RESIZE) {
				node.setLayoutY(node.getLayoutY() + e.getY());
				updatedRootSize = new Dimension2D(actualRootSize.getWidth(), actualRootSize.getHeight() - e.getY());
			} else if (node.getCursor() == Cursor.E_RESIZE) {
				updatedRootSize = new Dimension2D(e.getX(), actualRootSize.getHeight());
			} else if (node.getCursor() == Cursor.S_RESIZE) {
				updatedRootSize = new Dimension2D(actualRootSize.getWidth(), e.getY());
			} else if (node.getCursor() == Cursor.W_RESIZE) {
				node.setLayoutX(node.getLayoutX() + e.getX());
				updatedRootSize = new Dimension2D(actualRootSize.getWidth() - e.getX(), actualRootSize.getHeight());
			}

			nodeRootChild.setPrefSize(updatedRootSize.getWidth(), updatedRootSize.getHeight());
		});
	}

	public static void makeUnresizable(Node node) {
		node.setOnMouseMoved(null);
		node.setOnMouseDragged(null);
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

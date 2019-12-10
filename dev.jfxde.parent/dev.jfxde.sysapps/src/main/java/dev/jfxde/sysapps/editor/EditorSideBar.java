package dev.jfxde.sysapps.editor;

import java.util.HashMap;
import java.util.Map;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.layout.Region;

public class EditorSideBar extends Region {

    private static double WIDTH = 10;
    private Map<Integer, Node> marks = new HashMap<>();
    private GenericStyledArea<?, ?, ?> area;

    public EditorSideBar(GenericStyledArea<?, ?, ?> area) {
        this.area = area;

        setPrefWidth(WIDTH);
    }

    public void addMark(int line, Node mark) {
        marks.put(line, mark);
        getChildren().add(mark);

        mark.setOnMousePressed(e -> {
            area.moveTo(line, 0);
            area.requestFollowCaret();
            Platform.runLater(() -> area.requestFocus());
        });
    }

    public void removeMark(int line) {
        getChildren().remove(marks.remove(line));
    }

    @Override
    protected void layoutChildren() {

        double height = Math.min(area.getTotalHeightEstimate(), getHeight()) / area.getParagraphs().size();
        double markHeight = Math.max(height - 2, 3);

        for (int i : marks.keySet()) {

            double y = i * height;
            Node mark = marks.get(i);

            layoutInArea(mark, 0, y, WIDTH - 2, markHeight, 0, new Insets(0), HPos.CENTER, VPos.CENTER);
        }
    }
}

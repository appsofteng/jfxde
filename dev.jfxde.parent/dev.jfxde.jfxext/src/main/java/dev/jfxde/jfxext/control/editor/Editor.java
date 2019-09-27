package dev.jfxde.jfxext.control.editor;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.GenericStyledArea;

import javafx.scene.layout.StackPane;

public class Editor<T extends GenericStyledArea<?, ?, ?>> extends StackPane  {

    private T area;
    List<Behavior<T>> behaviors = new ArrayList<>();

    public Editor(T area) {
        this.area = area;
    }

    public T getArea() {
        return area;
    }

    public Editor<T> add(Behavior<T> behavior) {
        behavior.setEditor(this);
        behaviors.add(behavior);
        return this;
    }

    public Editor<T> add(List<Behavior<T>> behaviors) {
        behaviors.forEach(b -> b.setEditor(this));
        this.behaviors.addAll(behaviors);
        return this;
    }
}

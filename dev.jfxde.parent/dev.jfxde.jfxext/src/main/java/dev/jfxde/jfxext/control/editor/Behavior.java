package dev.jfxde.jfxext.control.editor;

import org.fxmisc.richtext.GenericStyledArea;

public abstract class Behavior<T extends GenericStyledArea<?, ?, ?>> {

    protected Editor<T> editor;
    protected T area;

    public T getArea() {
        return area;
    }

    public void setEditor(Editor<T> editor) {
        this.editor = editor;
        this.area = editor.getArea();
        setBehavior();
    }

    protected abstract void setBehavior();
}

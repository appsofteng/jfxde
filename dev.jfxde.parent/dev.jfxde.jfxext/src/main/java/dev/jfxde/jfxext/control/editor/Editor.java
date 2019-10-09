package dev.jfxde.jfxext.control.editor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.fxmisc.richtext.GenericStyledArea;

public class Editor<T extends GenericStyledArea<?, ?, ?>> {

    private T area;
    private Map<Class<?>, Feature<T>> features = new HashMap<>();

    public Editor(T area) {
        this.area = area;
    }

    public void init() {
        features.values().forEach(Feature::init);
    }

    public T getArea() {
        return area;
    }

    public Editor<T> add(Feature<T> feature) {
        feature.setEditor(this);
        features.put(feature.getClass(), feature);
        return this;
    }

    public Editor<T> add(List<Feature<T>> features) {
        features.forEach(b -> {
            b.setEditor(this);
            this.features.put(b.getClass(), b);
        });
        return this;
    }

    @SuppressWarnings("unchecked")
    public <F extends Feature<T>> F getFeature(Class<F> cls) {
        return (F) features.get(cls);
    }
}

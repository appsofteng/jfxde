package dev.jfxde.jfxext.richtextfx.features;

import java.util.ArrayList;
import java.util.List;

import org.fxmisc.richtext.GenericStyledArea;

public class AreaFeatures<T extends GenericStyledArea<?, ?, ?>> {

    private T area;
    private List<Feature<T>> features = new ArrayList<>();

    private AreaFeatures(T area) {
        this.area = area;
    }

    public static <S extends GenericStyledArea<?, ?, ?>> AreaFeatures<S> decorate(S area) {
        return new AreaFeatures<>(area);
    }

    public AreaFeatures<T> add(Feature<T> feature) {
        features.add(feature);
        feature.setArea(area);
        area.getProperties().put(feature.getClass(), feature);
        return this;
    }

    public T init() {
        features.forEach(f -> f.init());
        return area;
    }
}

package dev.jfxde.fxmisc.richtext.extensions;

import java.util.HashMap;
import java.util.Map;

import org.fxmisc.richtext.GenericStyledArea;

public class AreaExtensions<T extends GenericStyledArea<?, ?, ?>> {

    private T area;
    private Map<Class<?>, AreaExtension<T>> extensions = new HashMap<>();

    private AreaExtensions(T area) {
        this.area = area;
    }

    public static <S extends GenericStyledArea<?, ?, ?>> AreaExtensions<S> decorate(S area) {
        return new AreaExtensions<>(area);
    }

    public AreaExtensions<T> add(AreaExtension<T> extension) {
        extensions.put(extension.getClass(), extension);
        extension.setAreaFeatures(this);
        extension.setArea(area);
        return this;
    }

    public T init() {
        extensions.values().forEach(f -> f.init());
        return area;
    }

    <S extends AreaExtension<T>> S getExtension(Class<S> cls) {
        return (S) extensions.get(cls);
    }
}

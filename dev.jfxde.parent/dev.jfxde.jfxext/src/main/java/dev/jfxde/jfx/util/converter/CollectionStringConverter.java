package dev.jfxde.jfx.util.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import javafx.util.StringConverter;

public class CollectionStringConverter extends StringConverter<Collection<String>> {

    @Override
    public String toString(Collection<String> object) {
        return object.stream().collect(Collectors.joining(","));
    }

    @Override
    public Collection<String> fromString(String string) {
        return Arrays.asList(string.split(","));
    }
}

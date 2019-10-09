package dev.jfxde.jfxext.control.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Token {

    private List<String> style = new ArrayList<>();
    private final String value;
    private int start;
    private int end;
    private Pattern pattern;
    private BiFunction<Matcher,String,Token> tokenFunction;

    public Token(String styleClass, String value) {
        style.add(styleClass);
        this.value = value;
    }

    public Token(String value, Pattern pattern, BiFunction<Matcher,String,Token> tokenFunction) {
        this.value = value;
        this.pattern = pattern;
        this.tokenFunction = tokenFunction;
    }

    public Token(String styleClass, String value, Pattern pattern, BiFunction<Matcher,String,Token> tokenFunction) {
        style.add(styleClass);
        this.value = value;
        this.pattern = pattern;
        this.tokenFunction = tokenFunction;
    }

    public Token(List<String> style, String value) {
        this.style.addAll(style);
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public List<String> getStyle() {
        return style;
    }

    public String getStyleClass() {
        return style.isEmpty() ? "" : style.get(0);
    }

    public void setStyleClass(String styleClass) {
        style.clear();
        style.add(styleClass);
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    public Pattern getPattern() {
        return pattern;
    }

    public BiFunction<Matcher,String,Token> getTokenFunction() {
        return tokenFunction;
    }

    @Override
    public String toString() {
        return "style " + style + " value " + value + " start " + start + " end " + end + " pattern " + pattern;
    }
}
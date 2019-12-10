package dev.jfxde.jx.tools;

import java.util.ArrayList;
import java.util.List;

public class Token {

    private int start;
    private int end;
    private String type;
    private String value;
    private int length;
    private Token oppositeToken;
    private List<String> style = new ArrayList<>();

    public Token() {
    }

    public Token(int start, int end, String type, String value) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.value = value;
        this.length = end - start;
        resetStyle();
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    public Token getOppositeToken() {
        return oppositeToken;
    }

    public int getLength() {
        return length;
    }

    public List<String> getStyle() {
        return style;
    }

    public boolean isOnCaretPosition(int caretPosition) {
        return start <= caretPosition && caretPosition <= end;
    }

    public boolean isDelimiter() {
        return (isOpen(type) || isClose(type)) && oppositeToken != null;
    }

    public void setOppositeToken(Token oppositeToken) {
        this.oppositeToken = oppositeToken;
    }

    public boolean isClose() {
        return isClose(type);
    }

    static boolean isOpen(String type) {
        return type.toLowerCase().endsWith("open");
    }

    static boolean isClose(String type) {
        return type.toLowerCase().endsWith("close");
    }

    public void resetStyle() {
        style.clear();
        style.add(type.toLowerCase());
    }
}

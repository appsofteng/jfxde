package dev.jfxde.fxmisc.richtext;

public class Token {

    private int start;
    private int end;
    private String type;
    private String value;
    private int length;
    private Token oppositeToken;

    public Token(int start, int end, String type, String value) {
        this.start = start;
        this.end = end;
        this.type = type;
        this.value = value;
        this.length = end - start;
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

    public boolean isWithin(int index) {
        return start <= index && index < end;
    }

    public void setOppositeToken(Token oppositeToken) {
        this.oppositeToken = oppositeToken;
    }
}

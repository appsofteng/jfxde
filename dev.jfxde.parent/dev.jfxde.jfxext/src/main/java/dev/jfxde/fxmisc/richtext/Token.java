package dev.jfxde.fxmisc.richtext;

public class Token {

    private int start;
    private int end;
    private String type;
    private String value;
    private int length;

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

    public String getType() {
        return type;
    }

    public int getLength() {
        return length;
    }
}

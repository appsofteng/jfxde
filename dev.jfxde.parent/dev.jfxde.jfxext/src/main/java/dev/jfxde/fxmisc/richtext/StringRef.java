package dev.jfxde.fxmisc.richtext;

public class StringRef {

    private int line;
    private int start;
    private int end;
    private String value;

    public StringRef(int line, int start, int end, String value) {
        this.line = line;
        this.start = start;
        this.end = end;
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (line + 1) + "," + (start + 1) + ": " + value;
    }
}

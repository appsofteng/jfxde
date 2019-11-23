package dev.jfxde.fxmisc.richtext;

public class StringRef {

    private int line;
    private int column;
    private String value;

    public StringRef(int line, int column, String value) {
        this.line = line;
        this.column = column;
        this.value = value;
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getValue() {
        return value;
    }
}

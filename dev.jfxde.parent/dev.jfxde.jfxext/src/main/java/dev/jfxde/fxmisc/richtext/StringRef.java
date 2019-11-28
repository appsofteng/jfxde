package dev.jfxde.fxmisc.richtext;

public class StringRef {

    private Line line;
    private int start;
    private String value;

    public StringRef(Line line, int start, String value) {
        this.line = line;
        this.start = start;
        this.value = value;
    }

    public Line getLine() {
        return line;
    }

    public int getStart() {
        return start;
    }

    public String getValue() {
        return value;
    }

    @Override
    public String toString() {
        return (line.getNumber() + 1) + "," + (start + 1) + ": " + line.getValue().trim();
    }
}

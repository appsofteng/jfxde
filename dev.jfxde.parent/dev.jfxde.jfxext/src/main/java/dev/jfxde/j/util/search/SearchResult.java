package dev.jfxde.j.util.search;

public class SearchResult {

    private Line line;
    private int column;
    private String value;
    private int start;
    private int end;

    public SearchResult(Line line, int column, String value) {
        this.line = line;
        this.column = column;
        this.value = value;
        this.start = line.getStart() + column;
        this.end = start + value.length();
    }

    public Line getLine() {
        return line;
    }

    public int getColumn() {
        return column;
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
        return (line.getIndex() + 1) + "," + (column + 1) + ": " + line.getValue().trim();
    }
}

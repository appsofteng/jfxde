package dev.jfxde.j.util.search;

public class Line {

    private String value;
    private int index;
    private int start;
    private int end;

    public Line(String value, int index, int start) {

        this.value = value;
        this.index = index;
        this.start = start;
        this.end = start + value.length();
    }

    public String getValue() {
        return value;
    }

    public int getIndex() {
        return index;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Line createNext(String value) {
        value += "\n";
        return new Line(value, index + 1, end);
    }

    public boolean contains(int index) {
        return start <= index && index < end;
    }

    public String getUpper(int index) {
        String upper = value.substring(value.length() - (end - index));

        return upper;
    }

    public String getLower(int index) {
        String upper = value.substring(0, value.length() - (end - index));

        return upper;
    }

    public void appendEOL() {
        if (index > 0) {

        }
    }
}

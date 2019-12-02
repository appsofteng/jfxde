package dev.jfxde.j.util.search;

public class Line {

    private String value;
    private int number;
    private int start;
    private int end;

    public Line(String value, int number, int start) {

        this.value = value;
        this.number = number;
        this.start = start;
        this.end = start + value.length();
    }

    public String getValue() {
        return value;
    }

    public int getNumber() {
        return number;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public Line createNext(String value) {

        return new Line(value, number + 1, end);
    }

    public boolean contains(int index) {
        return start <= index && index < end;
    }
}

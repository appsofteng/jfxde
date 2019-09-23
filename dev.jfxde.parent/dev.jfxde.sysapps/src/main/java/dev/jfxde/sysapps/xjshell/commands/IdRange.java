package dev.jfxde.sysapps.xjshell.commands;

public class IdRange {

    private final int start;
    private final int end;

    public IdRange(String value) {
        String[] parts = value.split("-");
        this.start = Integer.parseInt(parts[0]);
        this.end = Integer.parseInt(parts[1]);
    }

    public boolean inside(String value) {
        int id = Integer.parseInt(value);
        return id >= start && id <= end;
    }
}

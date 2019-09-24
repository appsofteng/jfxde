package dev.jfxde.sysapps.jshell;

public abstract class CompletionItem implements Comparable<CompletionItem> {

    private String documentation;

    public abstract void complete();

    public String getDocumentation() {
        return documentation;
    }

    @Override
    public int compareTo(CompletionItem o) {
        return toString().compareTo(o.toString());
    }
}

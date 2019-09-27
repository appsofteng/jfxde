package dev.jfxde.jfxext.control.editor;

public abstract class CompletionItem implements Comparable<CompletionItem> {

    private String documentation;

    public abstract void complete();

    public String getDocumentation() {

        if (documentation == null) {
            documentation = loadDocumentation();
        }

        return documentation;
    }

    protected String loadDocumentation() {
        return "";
    }

    @Override
    public int compareTo(CompletionItem o) {
        return toString().compareTo(o.toString());
    }
}

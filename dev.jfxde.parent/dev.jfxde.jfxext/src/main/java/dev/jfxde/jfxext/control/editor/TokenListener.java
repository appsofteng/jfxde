package dev.jfxde.jfxext.control.editor;

public interface TokenListener {

    void onLevelIncreased();
    void onLevelDecreased();
    void process(Token token);
}

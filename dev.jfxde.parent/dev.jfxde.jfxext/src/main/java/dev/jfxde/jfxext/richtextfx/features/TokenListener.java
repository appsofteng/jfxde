package dev.jfxde.jfxext.richtextfx.features;

public interface TokenListener {

    void onLevelIncreased();
    void onLevelDecreased();
    void process(Token token);
}

package dev.jfxde.fxmisc.richtext.features;

public interface TokenListener {

    void onLevelIncreased();
    void onLevelDecreased();
    void process(Token token);
}

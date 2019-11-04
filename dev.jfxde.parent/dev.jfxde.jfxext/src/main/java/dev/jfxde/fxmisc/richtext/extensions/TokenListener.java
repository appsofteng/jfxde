package dev.jfxde.fxmisc.richtext.extensions;

public interface TokenListener {

    void onLevelIncreased();
    void onLevelDecreased();
    void process(Token token);
}

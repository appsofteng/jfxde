package dev.jfxde.logic.data;

import java.util.logging.Level;

public class ConsoleOutput {

    private final String text;
    private final Type type;

    public ConsoleOutput(String text) {
        this(text, Type.NORMAL);
    }

    public ConsoleOutput(String text, Type type) {
        this.text = text;
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public Type getType() {
        return type;
    }

    public boolean isEof() {
        return "\n".equals(text);
    }

    public enum Type {
        NORMAL(Level.INFO), COMMENT(Level.INFO), ERROR(Level.SEVERE);

        private final Level level;

        private Type(Level level) {
            this.level = level;
        }

        public Level getLevel() {
            return level;
        }
    }
}

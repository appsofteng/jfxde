package dev.jfxde.logic.data;

import java.util.logging.Level;

public class ConsoleOutput {

	private final String text;
	private final Type err;

	public ConsoleOutput(String text, Type type) {
		this.text = text;
		this.err = type;
	}

	public String getText() {
		return text;
	}

	public Type getType() {
		return err;
	}

	public boolean isEof() {
		return "\n".equals(text);
	}

	public enum Type {
		NORMAL(Level.INFO), ERROR(Level.SEVERE);

		private final Level level;

		private Type(Level level) {
			this.level = level;
		}


		public Level getLevel() {
			return level;
		}
	}
}

package dev.jfxde.logic;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConsoleManager extends Manager {

	private static final int LINE_LIMIT = 500;
	private int lineCount;

	public static final PrintStream out = System.out;
	public static final PrintStream err = System.err;

	private PrintStream cout = new PrintStream(new ConsoleOutputStream(Type.NORMAL, out), true);
	private PrintStream cerr = new PrintStream(new ConsoleOutputStream(Type.ERROR, err), true);

	private ObservableList<ConsoleOutput> outputs = FXCollections.observableArrayList();

	private static final Logger LOGGER = Logger.getLogger(ConsoleManager.class.getName());

	private boolean systemOutput = true;

	public ConsoleManager() {

	}

	public ConsoleManager(boolean systemOutput) {
		this.systemOutput = systemOutput;
	}

	@Override
	void init() throws Exception {

		System.setOut(cout);
		System.setErr(cerr);
	}

	public PrintStream getCout() {
		return cout;
	}

	public PrintStream getCerr() {
		return cerr;
	}

	public ObservableList<ConsoleOutput> getOutputs() {
		return outputs;
	}

	public synchronized List<ConsoleOutput> getCopyOutputs() {
		return new ArrayList<>(outputs);
	}

	public synchronized void clear() {
		outputs.clear();
		lineCount = 0;
	}

	private synchronized void update(String msg, Type type) {

		// Windows OS
		msg = msg.replace("\r", "");
		List<ConsoleOutput> newOutputs = Stream.of(msg.split("((?<=\n)|(?=\n))")).map(s -> new ConsoleOutput(s, type))
				.collect(Collectors.toList());

		outputs.addAll(newOutputs);

		lineCount += newOutputs.stream().filter(ConsoleOutput::isEof).count();
		removeLines();

	}

	private void removeLines() {

		if (lineCount <= LINE_LIMIT) {
			return;
		}

		int count = lineCount - LINE_LIMIT;
		lineCount = LINE_LIMIT;

		while (count > 0 && !outputs.isEmpty()) {

			List<ConsoleOutput> line = new ArrayList<>();

			for (ConsoleOutput o : outputs) {
				line.add(o);
				if (o.isEof()) {
					count--;
					break;
				}
			}

			outputs.removeAll(line);
		}
	}

	private class ConsoleOutputStream extends ByteArrayOutputStream {

		private final Type type;
		private PrintStream pstream;

		public ConsoleOutputStream(Type type, PrintStream pstream) {
			this.type = type;
			this.pstream = pstream;
		}

		@Override
		public synchronized void flush() throws IOException {

			String msg = toString();

			if (msg.isEmpty()) {
				return;
			}

			if (systemOutput) {
				pstream.print(msg);
				LOGGER.log(type.getLevel(), msg);
			}

			update(msg, type);
			reset();
		}
	}
}

package dev.jfxde.logic;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import dev.jfxde.jfxext.control.ConsoleModel;
import dev.jfxde.jfxext.richtextfx.TextStyleSpans;
import javafx.collections.ListChangeListener.Change;

public class ConsoleManager extends Manager {

	public static final PrintStream out = System.out;
	public static final PrintStream err = System.err;

	private ConsoleModel consoleModel = new ConsoleModel();

	@Override
	void init() throws Exception {

	    System.setIn(consoleModel.getIn());
		System.setOut(consoleModel.getOut());
		System.setErr(consoleModel.getErr());

        consoleModel.getOutput().addListener((Change<? extends TextStyleSpans> c) -> {

            while (c.next()) {

                if (c.wasAdded()) {
                    List<? extends TextStyleSpans> added = new ArrayList<>(c.getAddedSubList());
                    added.stream().map(TextStyleSpans::getText).forEach(out::print);
                }
            }
        });

	}

	public ConsoleModel getConsoleModel() {
        return consoleModel;
    }
}

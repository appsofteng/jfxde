package dev.jfxde.sysapps.jshell;

import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import jdk.jshell.JShell;

public abstract class JShellOutput {

    protected AppContext context;
    protected JShell jshell;
    protected CodeArea outputArea;
    protected List<String> history;

    JShellOutput(AppContext context, JShell jshell, CodeArea outputArea) {
      this(context, jshell, outputArea, List.of());
    }

    JShellOutput(AppContext context, JShell jshell, CodeArea outputArea, List<String> history) {
        this.context = context;
        this.jshell = jshell;
        this.outputArea = outputArea;
        this.history = history;
    }

    abstract void output(String input);

}

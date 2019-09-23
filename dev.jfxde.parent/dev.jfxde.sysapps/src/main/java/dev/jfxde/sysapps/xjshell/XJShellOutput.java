package dev.jfxde.sysapps.xjshell;

import java.util.List;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import jdk.jshell.JShell;

public abstract class XJShellOutput {

    protected AppContext context;
    protected JShell jshell;
    protected CodeArea outputArea;
    protected List<String> history;

    XJShellOutput(AppContext context, JShell jshell, CodeArea outputArea) {
      this(context, jshell, outputArea, List.of());
    }

    XJShellOutput(AppContext context, JShell jshell, CodeArea outputArea, List<String> history) {
        this.context = context;
        this.jshell = jshell;
        this.outputArea = outputArea;
        this.history = history;
    }

    abstract void output(String input);

}

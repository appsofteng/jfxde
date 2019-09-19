package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.api.AppContext;
import jdk.jshell.JShell;

public abstract class JShellOutput {

    protected AppContext context;
    protected JShell jshell;
    protected CodeArea outputArea;

    JShellOutput(AppContext context, JShell jshell, CodeArea outputArea) {
        this.context = context;
        this.jshell = jshell;
        this.outputArea = outputArea;
    }

    abstract void output(String input);

}

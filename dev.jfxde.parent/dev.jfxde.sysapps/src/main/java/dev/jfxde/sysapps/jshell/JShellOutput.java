package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.JShell;

public abstract class JShellOutput {

    protected JShell jshell;
    protected CodeArea outputArea;

    JShellOutput(JShell jshell, CodeArea outputArea) {
        this.jshell = jshell;
        this.outputArea = outputArea;
    }

    abstract void output(String input);

}

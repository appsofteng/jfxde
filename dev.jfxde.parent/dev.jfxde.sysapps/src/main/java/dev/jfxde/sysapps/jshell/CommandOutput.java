package dev.jfxde.sysapps.jshell;

import org.fxmisc.richtext.CodeArea;

import jdk.jshell.JShell;

public class CommandOutput extends JShellOutput{

    CommandOutput(JShell jshell, CodeArea outputArea) {
        super(jshell, outputArea);
    }

    @Override
    void output(String input) {

    }

}

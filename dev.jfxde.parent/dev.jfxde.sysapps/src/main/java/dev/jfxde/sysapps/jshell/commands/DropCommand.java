package dev.jfxde.sysapps.jshell.commands;

import org.fxmisc.richtext.CodeArea;

import dev.jfxde.logic.data.ConsoleOutput;
import dev.jfxde.logic.data.ConsoleOutput.Type;
import dev.jfxde.sysapps.jshell.SnippetUtils;
import dev.jfxde.sysapps.util.CodeAreaUtils;
import jdk.jshell.JShell;

public class DropCommand extends Command {

    public DropCommand(JShell jshell, CodeArea outputArea) {
        super("/drop", jshell, outputArea);
    }

    @Override
    public void execute(SnippetMatch input) {
        StringBuilder sb = new StringBuilder();
        jshell.snippets()
                .filter(s -> jshell.status(s).isActive())
                .filter(s -> input.matches(s))
                .forEach(s -> {
                    sb.append("dropped" + SnippetUtils.toString(s, jshell));
                    jshell.drop(s);
                });

        CodeAreaUtils.addOutputLater(outputArea, new ConsoleOutput(sb.toString() + "\n", Type.COMMENT));
    }
}

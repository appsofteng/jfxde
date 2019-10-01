package dev.jfxde.sysapps.jshell;

import java.util.function.BiFunction;

import jdk.jshell.JShell;
import jdk.jshell.Snippet;
import jdk.jshell.Snippet.Status;

public class IdGenerator implements BiFunction<Snippet, Integer, String> {

    private JShell jshell;
    private int maxId;

    public void setJshell(JShell jshell) {
        this.jshell = jshell;
    }

    public int getMaxId() {
        return maxId;
    }

    @Override
    public String apply(Snippet snippet, Integer id) {
        String newId = id.toString();

        if (id > maxId) {
            maxId = id;
        } else {
            Snippet existingSnippet = JShellUtils.getSnippet(jshell, id);

            if (existingSnippet != null && jshell.status(existingSnippet) == Status.DROPPED) {
                newId = String.valueOf(++maxId);
            }
        }

        return newId;
    }

}

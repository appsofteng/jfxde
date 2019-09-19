package dev.jfxde.sysapps.jshell.commands;

import java.util.HashSet;
import java.util.Set;

import dev.jfxde.sysapps.jshell.SnippetUtils;
import jdk.jshell.Snippet;

public class SnippetMatch {

    private final Set<String> names = new HashSet<>();
    private final Set<String> ids = new HashSet<>();
    private final Set<IdRange> idRanges = new HashSet<>();

    public SnippetMatch(String[] values) {

        for (String value : values) {
            if (value.matches("\\d+")) {
                ids.add(value);
            } else if (value.matches("\\d+-\\d+")) {
                idRanges.add(new IdRange(value));
            } else {
                names.add(value);
            }
        }
    }

    public boolean matches(Snippet snippet) {

        return names.stream().anyMatch(n -> n.equals(SnippetUtils.getName(snippet)))
                || ids.stream().anyMatch(i -> i.equals(snippet.id()))
                || idRanges.stream().anyMatch(r -> r.inside(snippet.id()));
    }



}

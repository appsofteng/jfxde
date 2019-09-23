package dev.jfxde.sysapps.xjshell.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.sysapps.xjshell.SnippetUtils;
import jdk.jshell.JShell;
import jdk.jshell.Snippet;

public class SnippetMatch {

    private Map<String, Snippet> snippetsById = new HashMap<>();
    private Map<String, List<Snippet>> snippetsByName = new HashMap<>();

    public SnippetMatch(JShell jshell) {

        jshell.onSnippetEvent(e -> {

            if (e.snippet() == null || e.snippet().id() == null) {
                return;
            }

            String name = SnippetUtils.getName(e.snippet());

            snippetsById.put(e.snippet().id(), e.snippet());
            List<Snippet> snippets = snippetsByName.computeIfAbsent(name, k -> new ArrayList<>());
            snippets.add(e.snippet());

        });
    }

    public List<Snippet> matches(String[] values) {

        List<Snippet> snippets = new ArrayList<>();

        for (String value : values) {
            if (value.matches("\\d+")) {
                Snippet s = snippetsById.get(value);
                if (s != null) {
                    snippets.add(s);
                }
            } else if (value.matches("\\d+-\\d+")) {
                String[] parts = value.split("-");
                snippets.addAll(IntStream.rangeClosed(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]))
                        .mapToObj(i -> snippetsById.get(String.valueOf(i)))
                        .filter(s -> s != null)
                        .collect(Collectors.toList()));
            } else {
                snippets.addAll(snippetsByName.getOrDefault(value, List.of()));
            }
        }

        return snippets;
    }

}

package dev.jfxde.j.util.search;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public final class Searcher {

    private Line currectLine;
    private int searchWindowStart;
    private String searchWindow;

    private Searcher() {
    }

    public static Searcher get() {

        return new Searcher();
    }

    public void search(Stream<String> lines, Pattern pattern, Predicate<SearchResult> process) {
        search(lines, 0, Integer.MAX_VALUE, pattern, process);
    }

    public void search(Stream<String> lines, int from, int to, Pattern pattern, Predicate<SearchResult> process) {

        currectLine = new Line("", -1, 0);
        searchWindowStart = from;
        searchWindow = "";

        List<Line> searchLines = new ArrayList<>();

        lines.allMatch(line -> {
            boolean continueSearch = true;

            currectLine = currectLine.createNext(line);

            if (currectLine.contains(from)) {
                searchWindow += currectLine.getUpper(from);
            } else if (currectLine.contains(to)) {
                searchWindow += currectLine.getLower(to);
                continueSearch = false;
            } else if (currectLine.getEnd() < from) {
                return true;
            } else {
                searchWindow += currectLine.getValue();
            }

            searchLines.add(currectLine);

            Line matchLine = null;
            Matcher matcher = pattern.matcher(searchWindow);
            int matchEnd = 0;

            while (matcher.find() && continueSearch) {
                int group = matcher.groupCount() == 1 ? 1 : 0;
                int absMatchStart = searchWindowStart + matcher.start(group);
                matchLine = searchLines.stream().filter(ln -> ln.contains(absMatchStart)).findFirst().orElse(null);
                SearchResult stringRef = new SearchResult(matchLine, absMatchStart - matchLine.getStart(), matcher.group(group));
                matchEnd = matcher.end(group);
                continueSearch = process.test(stringRef);
            }

            if (matchLine != null) {
                int matchLineNumber = matchLine.getIndex();
                searchLines.removeIf(ln -> ln.getIndex() < matchLineNumber);
            }

            if (matchEnd > 0) {
                searchWindow = searchWindow.substring(matchEnd);
                searchWindowStart += matchEnd;
            }

            return continueSearch;

        });
    }
}

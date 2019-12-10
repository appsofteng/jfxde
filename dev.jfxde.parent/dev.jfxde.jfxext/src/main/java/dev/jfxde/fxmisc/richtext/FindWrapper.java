package dev.jfxde.fxmisc.richtext;

import java.util.List;
import java.util.function.Supplier;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.fxmisc.richtext.StyleClassedTextArea;

import dev.jfxde.j.util.search.Searcher;
import dev.jfxde.j.util.search.SearchResult;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.IndexRange;
import javafx.util.Pair;

public class FindWrapper extends StyleClassedTextAreaWrapper {

    private static final String FIND_STYLE = "jd-find";
    private static final String FIND_STYLE_SELECTED = "jd-find-selected";
    private Pattern pattern;
    private IntegerProperty index = new SimpleIntegerProperty(-1);
    private ObservableList<SearchResult> searchResults = FXCollections.observableArrayList();
    private ReadOnlyStringWrapper foundCount = new ReadOnlyStringWrapper();
    private boolean inSelection;
    private IndexRange selection;
    private boolean replace;
    private boolean replaceAll;

    public FindWrapper(StyleClassedTextArea area) {
        super(area);

        area.textProperty().addListener((v, o, n) -> reset());
        foundCount.bind(Bindings.createStringBinding(() -> getIndex() + 1 + "/" + searchResults.size(), index, searchResults));
    }

    public ObservableList<SearchResult> getSearchResults() {
        return searchResults;
    }

    void afterReplace() {
        if (replace) {
            replace = false;
            var pattern = this.pattern;
            this.pattern = null;

            find(pattern, inSelection);
        }
    }

    public void reset() {
        if (!replace && !replaceAll) {
            searchResults.clear();
            setIndex(-1);
            pattern = null;
            inSelection = false;
            selection = null;
            replace = false;
            replaceAll = false;
        }
    }

    private int getIndex() {
        return index.get();
    }

    private void setIndex(int value) {
        index.set(value);
    }

    public ReadOnlyStringProperty foundCountProperty() {
        return foundCount.getReadOnlyProperty();
    }

    void findWord() {
        area.requestFocus();
        Pair<IndexRange, String> pair = getCodeWord(getArea().getText(), getArea().getCaretPosition());
        area.selectRange(pair.getKey().getStart(), pair.getKey().getEnd());
        String word = pair.getValue();

        Pattern pattern = null;

        if (word != null && !word.isBlank()) {
            pattern = Pattern.compile("(?<=^|\\W)(" + word + ")(?=\\W|$)");
        }

        find(pattern, false);
    }

    public void findPrevious(Pattern pattern, boolean inSelection) {

        findAnother(pattern, inSelection, this::findPrevious);
    }

    public void findNext(Pattern pattern, boolean inSelection) {

        findAnother(pattern, inSelection, this::findNext);
    }

    public void replace(String text) {
        if (getIndex() >= 0 && getIndex() < searchResults.size()) {

            replace = true;
            SearchResult range = searchResults.get(getIndex());
            getArea().replaceText(range.getStart(), range.getEnd(), text);
        }
    }

    public void replaceAll(String text) {
        int length = getArea().getLength();
        int delta = 0;

        replaceAll = true;

        for (var range : searchResults) {
            getArea().replaceText(range.getStart() + delta, range.getEnd() + delta, text);

            delta = getArea().getLength() - length;
        }

        setIndex(searchResults.size() - 1);
    }

    private void findAnother(Pattern pattern, boolean inSelection, Supplier<Pair<Integer, SearchResult>> finder) {

        var selection = getArea().getSelection();

        if (inSelection && selection.getLength() > 0) {
            this.selection = selection;
        }

        find(pattern, inSelection);

        if (searchResults.isEmpty()) {
            return;
        }

        if (getIndex() >= 0 && getIndex() < searchResults.size()) {
            var range = searchResults.get(getIndex());
            removeStyle(range.getStart(), range.getEnd(), List.of(FIND_STYLE_SELECTED));
        }

        Pair<Integer, SearchResult> pair = finder.get();
        SearchResult range = pair.getValue();
        setIndex(pair.getKey());

        getArea().moveTo(range.getStart());
        getArea().requestFollowCaret();

        addStyle(range.getStart(), range.getEnd(), List.of(FIND_STYLE_SELECTED));

        area.requestFocus();
    }

    private Pair<Integer, SearchResult> findPrevious() {

        Pair<Integer, SearchResult> pair = Stream.iterate(searchResults.size() - 1, i -> i >= 0, i -> i - 1)
                .filter(i -> searchResults.get(i).getStart() < getArea().getCaretPosition())
                .map(i -> new Pair<>(i, searchResults.get(i)))
                .findFirst()
                .orElse(new Pair<>(searchResults.size() - 1, searchResults.get(searchResults.size() - 1)));

        return pair;
    }

    private Pair<Integer, SearchResult> findNext() {

        Pair<Integer, SearchResult> pair = Stream.iterate(0, i -> i < searchResults.size(), i -> i + 1)
                .filter(i -> searchResults.get(i).getStart() > getArea().getCaretPosition())
                .findFirst()
                .map(i -> new Pair<>(i, searchResults.get(i)))
                .orElse(new Pair<>(0, searchResults.get(0)));

        return pair;
    }

    private void find(Pattern pattern, boolean inSelection) {

        if (replaceAll) {
            replaceAll = false;
            reset();
        }

        if (pattern != null && this.pattern != null && pattern.pattern().equals(this.pattern.pattern()) && pattern.flags() == this.pattern.flags()
                && inSelection == this.inSelection) {
            return;
        }

        this.pattern = pattern;
        this.inSelection = inSelection;
        searchResults.forEach(i -> removeStyle(i.getStart(), i.getEnd(), List.of(FIND_STYLE, FIND_STYLE_SELECTED)));
        searchResults.clear();

        if (pattern != null) {

            Searcher.get().search(area.getText().lines(), getTextStart(inSelection), getTextEnd(inSelection), pattern, r -> {
                addStyle(r.getStart(), r.getEnd(), List.of(FIND_STYLE));
                searchResults.add(r);
                return true;
            });
        }

        if (searchResults.isEmpty()) {
            setIndex(-1);
        }
    }

    private int getTextStart(boolean inSelection) {
        return inSelection && selection != null ? selection.getStart() : 0;
    }

    private int getTextEnd(boolean inSelection) {
        return inSelection && selection != null ? selection.getEnd() : area.getLength();
    }
}

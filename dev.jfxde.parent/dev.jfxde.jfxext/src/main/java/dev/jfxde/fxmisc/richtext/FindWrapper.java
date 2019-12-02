package dev.jfxde.fxmisc.richtext;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.fxmisc.richtext.StyleClassedTextArea;

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
    private ObservableList<IndexRange> indices = FXCollections.observableArrayList();
    private ReadOnlyStringWrapper foundCount = new ReadOnlyStringWrapper();
    private boolean inSelection;
    private IndexRange selection;
    private boolean replace;
    private boolean replaceAll;

    public FindWrapper(StyleClassedTextArea area) {
        super(area);

        area.textProperty().addListener((v, o, n) -> reset());
        foundCount.bind(Bindings.createStringBinding(() -> getIndex() + 1 + "/" + indices.size(), index, indices));
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
            indices.clear();
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
        if (getIndex() >= 0 && getIndex() < indices.size()) {

            replace = true;
            IndexRange range = indices.get(getIndex());
            getArea().replaceText(range, text);
        }
    }

    public void replaceAll(String text) {
        int length = getArea().getLength();
        int delta = 0;

        replaceAll = true;

        for (var range : indices) {
            getArea().replaceText(range.getStart() + delta, range.getEnd() + delta, text);

            delta = getArea().getLength() - length;
        }

        setIndex(indices.size() - 1);
    }

    private void findAnother(Pattern pattern, boolean inSelection, Supplier<Pair<Integer, IndexRange>> finder) {

        var selection = getArea().getSelection();

        if (inSelection && selection.getLength() > 0) {
            this.selection = selection;
        }

        find(pattern, inSelection);

        if (indices.isEmpty()) {
            return;
        }

        if (getIndex() >= 0 && getIndex() < indices.size()) {
            removeStyle(indices.get(getIndex()), List.of(FIND_STYLE_SELECTED));
        }

        Pair<Integer, IndexRange> pair = finder.get();
        IndexRange range = pair.getValue();
        setIndex(pair.getKey());

        getArea().moveTo(range.getStart());
        getArea().requestFollowCaret();

        addStyle(range, List.of(FIND_STYLE_SELECTED));

        area.requestFocus();
    }

    private Pair<Integer, IndexRange> findPrevious() {

        Pair<Integer, IndexRange> pair = Stream.iterate(indices.size() - 1, i -> i >= 0, i -> i - 1)
                .filter(i -> indices.get(i).getStart() < getArea().getCaretPosition())
                .map(i -> new Pair<>(i, indices.get(i)))
                .findFirst()
                .orElse(new Pair<>(indices.size() - 1, indices.get(indices.size() - 1)));

        return pair;
    }

    private Pair<Integer, IndexRange> findNext() {

        Pair<Integer, IndexRange> pair = Stream.iterate(0, i -> i < indices.size(), i -> i + 1)
                .filter(i -> indices.get(i).getStart() > getArea().getCaretPosition())
                .findFirst()
                .map(i -> new Pair<>(i, indices.get(i)))
                .orElse(new Pair<>(0, indices.get(0)));

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
        indices.forEach(i -> removeStyle(i, List.of(FIND_STYLE, FIND_STYLE_SELECTED)));
        indices.clear();

        if (pattern != null) {

            find(pattern, inSelection, r -> addStyle(r, List.of(FIND_STYLE)));
        }

        if (indices.isEmpty()) {
            setIndex(-1);
        }
    }

    private void find(Pattern pattern, boolean inSelection, Consumer<IndexRange> consumer) {

        Matcher matcher = pattern.matcher(getText(inSelection));
        int textStart = getTextStart(inSelection);

        while (matcher.find()) {
            int group = matcher.groupCount() == 1 ? 1 : 0;
            IndexRange range = new IndexRange(textStart + matcher.start(group), textStart + matcher.end(group));
            indices.add(range);
            consumer.accept(range);
        }
    }

    private String getText(boolean inSelection) {

        return inSelection && selection != null ? getArea().getText(selection) : getArea().getText();
    }

    private int getTextStart(boolean inSelection) {
        return inSelection && selection != null ? selection.getStart() : 0;
    }
}

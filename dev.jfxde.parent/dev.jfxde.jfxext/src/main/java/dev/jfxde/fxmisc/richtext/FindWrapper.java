package dev.jfxde.fxmisc.richtext;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public FindWrapper(StyleClassedTextArea area) {
        super(area);

        area.textProperty().addListener(o -> pattern = null);
        foundCount.bind(Bindings.createStringBinding(() -> index.get() + 1 + "/" + indices.size(), index, indices));
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

    private void findAnother(Pattern pattern, boolean inSelection, Supplier<IndexRange> finder) {

        boolean selected = !getArea().getSelectedText().isEmpty();
        IndexRange selection = getArea().getSelection();

        find(pattern, inSelection);

        if (indices.isEmpty()) {
            return;
        }

        if (index.get() >= 0 && index.get() < indices.size()) {
            removeStyle(indices.get(index.get()), List.of(FIND_STYLE_SELECTED));
        }

        IndexRange range = finder.get();

        if (selected) {
            getArea().showParagraphInViewport(getParagraphForAbsolutePosition(range.getStart()));
        } else {
          getArea().moveTo(range.getStart());
          getArea().requestFollowCaret();
        }

        addStyle(range, List.of(FIND_STYLE_SELECTED));

        area.requestFocus();

        if (selected && getArea().getSelectedText().isEmpty()) {
            getArea().selectRange(selection.getStart(), selection.getEnd());
        }
    }

    private IndexRange findPrevious() {

        index.set(index.get() - 1);
        if (index.get() < 0) {
            index.set(indices.size() - 1);
        }
        IndexRange range = indices.get(index.get());

        return range;
    }

    private IndexRange findNext() {

        index.set(index.get() + 1);

        if (index.get() >= indices.size()) {
            index.set(0);
        }

        IndexRange range = indices.get(index.get());

        return range;
    }

    private void find(Pattern pattern, boolean inSelection) {

        if (pattern != null && this.pattern != null && pattern.pattern().equals(this.pattern.pattern()) && pattern.flags() == this.pattern.flags()) {
            return;
        }

        this.pattern = pattern;
        indices.forEach(i -> removeStyle(i, List.of(FIND_STYLE, FIND_STYLE_SELECTED)));
        indices.clear();

        if (pattern == null) {
            index.set(-1);
        } else {

            find(pattern, inSelection, r -> addStyle(r, List.of(FIND_STYLE)));
        }
    }

    private String getText(boolean inSelection) {

        return inSelection ? getArea().getSelectedText() : getArea().getText();
    }

    private int getTextStart(boolean inSelection) {
        return inSelection ? getArea().getSelection().getStart() : 0;
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
}

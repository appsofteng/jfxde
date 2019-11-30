package dev.jfxde.fxmisc.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.scene.control.IndexRange;
import javafx.util.Pair;

public class FindWrapper extends StyleClassedTextAreaWrapper {

    private Pattern pattern;
    private IntegerProperty index = new SimpleIntegerProperty(-1);
    private List<IndexRange> indices = List.of();

    public FindWrapper(StyleClassedTextArea area) {
        super(area);

        area.textProperty().addListener(o -> pattern = null);
    }

    public void findPrevious(Pattern pattern) {

        find(pattern);
        findPrevious();
        area.requestFocus();
    }

    public void findNext(Pattern pattern) {

        find(pattern);
        findNext();
        area.requestFocus();
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

        find(pattern);
    }

    private void findPrevious() {

        if (indices.isEmpty()) {
            return;
        }

        IndexRange range = Stream.iterate(indices.size() - 1, i -> i >= 0, i -> i - 1).map(i -> indices.get(i))
                .filter(r -> r.getStart() < getArea().getCaretPosition())
                .findFirst()
                .orElse(indices.get(indices.size() - 1));

        index.set(indices.indexOf(range));
        getArea().moveTo(range.getStart());
        getArea().requestFollowCaret();
    }

    private void findNext() {

        if (indices.isEmpty()) {
            return;
        }

        IndexRange range = Stream.iterate(0, i -> i < indices.size(), i -> i + 1).map(i -> indices.get(i))
            .filter(r -> r.getStart() > getArea().getCaretPosition())
            .findFirst()
            .orElse(indices.get(0));

        index.set(indices.indexOf(range));
        getArea().moveTo(range.getStart());
        getArea().requestFollowCaret();
    }

    private void find(Pattern pattern) {

        if (pattern != null && this.pattern != null && pattern.pattern().equals(this.pattern.pattern()) && pattern.flags() == this.pattern.flags()) {
            return;
        }

        this.pattern = pattern;
        indices.forEach(i -> removeStyleClass(i, "jd-find"));

        if (pattern == null) {
            indices = List.of();
        } else {

            indices = find(pattern, r -> addStyleClass(r, "jd-find"));
        }
    }

    private List<IndexRange> find(Pattern pattern, Consumer<IndexRange> consumer) {
        List<IndexRange> indices = new ArrayList<>();
        Matcher matcher = pattern.matcher(getArea().getText());

        while (matcher.find()) {
            int group = matcher.groupCount() == 1 ? 1 : 0;
            IndexRange range = new IndexRange(matcher.start(group), matcher.end(group));
            indices.add(range);
            consumer.accept(range);
        }

        return indices;
    }
}

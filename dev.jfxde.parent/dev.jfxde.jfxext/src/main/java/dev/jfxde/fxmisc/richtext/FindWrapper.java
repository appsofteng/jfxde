package dev.jfxde.fxmisc.richtext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.StyleClassedTextArea;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.IndexRange;
import javafx.util.Pair;

public class FindWrapper extends StyleClassedTextAreaWrapper {

    private StringProperty string = new SimpleStringProperty();
    private BooleanProperty caseSensitive = new SimpleBooleanProperty();
    private BooleanProperty regEx = new SimpleBooleanProperty();
    private List<IndexRange> indices = List.of();

    public FindWrapper(StyleClassedTextArea area) {
        super(area);
    }

    void findWord() {
        area.requestFocus();
        Pair<IndexRange, String> pair = getCodeWord(getArea().getText(), getArea().getCaretPosition());
        area.selectRange(pair.getKey().getStart(), pair.getKey().getEnd());
        String word = pair.getValue();

        indices.forEach(i -> removeStyleClass(i, "jd-find"));

        if (word != null && !word.isBlank()) {
            Pattern pattern = Pattern.compile("(?<=^|\\W)(" + word + ")(?=\\W|$)");
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

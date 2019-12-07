package dev.jfxde.fxmisc.richtext;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.IntFunction;

import org.fxmisc.richtext.GenericStyledArea;
import org.fxmisc.richtext.LineNumberFactory;

import javafx.scene.Node;

public class ParagraphGraphicFactory implements IntFunction<Node>{

    private IntFunction<Node> start;
    private List<BiFunction<Integer, Node, Node>> factories;

    public ParagraphGraphicFactory(GenericStyledArea<?, ?, ?> area, List<BiFunction<Integer, Node, Node>> factories) {
        this.start = LineNumberFactory.get(area);
        this.factories = factories;
    }

    @Override
    public Node apply(int value) {
        Node node = start.apply(value);

        for (var factory : factories) {
            node = factory.apply(value, node);
        }

        return node;
    }
}

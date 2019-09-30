package dev.jfxde.jfxext.control.editor;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.Collection;
import java.util.function.Function;

import org.fxmisc.wellbehaved.event.Nodes;

import dev.jfxde.jfxext.util.LayoutUtils;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;

public class CompletionPopup extends Tooltip {

    static final double DEFAULT_WIDTH = 450;
    static final double DEFAULT_HEIGHT = 200;
    private ListView<CompletionItem> itemView = new ListView<>();
    private DocPopup docPopup;

    private EventHandler<KeyEvent> handler = e -> {

        if (e.getCode() == KeyCode.ENTER) {
            e.consume();
            selected();
        } else if (e.getCode() == KeyCode.ESCAPE) {
            close();
            e.consume();
        } else if (e.getCode() == KeyCode.UP) {
            selectPrevious();
        } else if (e.getCode() == KeyCode.DOWN) {
            selectNext();
        }
    };

    public CompletionPopup(Collection<? extends CompletionItem> items, Function<DocRef, String> documentation) {
        docPopup = new DocPopup(documentation);
        // does not work well because it blocks mouse press events outside the popup
        // setAutoHide(true);

        // not working when the list inside the popup has the focus
        setHideOnEscape(false);

        itemView.setItems(FXCollections.observableArrayList(items));
        itemView.setFocusTraversable(false);

        setMinSize(10, 10);
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        StackPane pane = new StackPane(itemView);
        pane.setPadding(new Insets(5));
        setGraphic(pane);
        LayoutUtils.makeResizable(this, pane, 5);

        setBehavior();
    }

    private void setBehavior() {

        Nodes.addInputMap(itemView,
                sequence(consume(keyPressed(ENTER), e -> selected()),
                        consume(keyPressed(ESCAPE), e -> close()),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 1), e -> itemView.setFocusTraversable(true)),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> selected())));

        itemView.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {
            if (n != null) {
                if (docPopup.loadContent(n.getDocRef())) {
                    double offset = Screen.getPrimary().getBounds().getWidth() - getAnchorX() - getPrefWidth() > getAnchorX() ? getPrefWidth()
                            : -docPopup.getPrefWidth();

                    docPopup.show(this, getAnchorX() + offset, getAnchorY());
                }
            }
        });
    }

    private void select(int i) {
        itemView.getSelectionModel().select(i);
    }

    private void selectPrevious() {

        if (itemView.getSelectionModel().getSelectedIndex() == 0) {
            itemView.getSelectionModel().select(itemView.getItems().size() - 1);
        } else {
            itemView.getSelectionModel().selectPrevious();
        }

        itemView.scrollTo(itemView.getSelectionModel().getSelectedIndex());
    }

    private void selectNext() {
        if (itemView.getSelectionModel().getSelectedIndex() == itemView.getItems().size() - 1) {
            itemView.getSelectionModel().select(0);
        } else {
            itemView.getSelectionModel().selectNext();
        }

        itemView.scrollTo(itemView.getSelectionModel().getSelectedIndex());
    }

    public CompletionItem getSelection() {
        return itemView.getSelectionModel().getSelectedItem();
    }

    public ReadOnlyObjectProperty<CompletionItem> selectedItemProperty() {
        return itemView.getSelectionModel().selectedItemProperty();
    }

    public void close() {
        itemView.getSelectionModel().clearSelection();
        getOwnerNode().removeEventFilter(KeyEvent.KEY_PRESSED, handler);
        hide();
    }

    public void selected() {
        getOwnerNode().removeEventFilter(KeyEvent.KEY_PRESSED, handler);
        hide();
    }

    @Override
    public void hide() {
        docPopup.hide();
        super.hide();
    }

    @Override
    public void show(Node ownerNode, double anchorX, double anchorY) {

        ownerNode.focusedProperty().addListener((v, o, n) -> {

            if (!n) {
                close();
            }
        });

        ownerNode.getScene().getWindow().xProperty().addListener((wv, wo, wn) -> {
            close();
        });

        ownerNode.getScene().getWindow().yProperty().addListener((wv, wo, wn) -> {
            close();
        });

        ownerNode.addEventFilter(KeyEvent.KEY_PRESSED, handler);

        super.show(ownerNode, anchorX, anchorY);

        select(0);
    }
}

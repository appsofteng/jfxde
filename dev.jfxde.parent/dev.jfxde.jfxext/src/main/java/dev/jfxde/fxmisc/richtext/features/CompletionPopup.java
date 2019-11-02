package dev.jfxde.fxmisc.richtext.features;

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

import dev.jfxde.jfx.scene.layout.LayoutUtils;
import javafx.beans.value.ChangeListener;
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
    private ChangeListener<Boolean> focusListener;
    private ChangeListener<Number> windowListener;

    private EventHandler<KeyEvent> handler;

    public CompletionPopup(Collection<? extends CompletionItem> items, Function<DocRef, String> documentation) {
        docPopup = new DocPopup(documentation);
        // does not work well because it blocks mouse press events outside the popup
        // setAutoHide(true);

        // not working when the list inside the popup has the focus
        setHideOnEscape(false);

        itemView.setItems(FXCollections.observableArrayList(items));

        setMinSize(10, 10);
        setPrefSize(DEFAULT_WIDTH, DEFAULT_HEIGHT);

        StackPane pane = new StackPane(itemView);
        pane.setPadding(new Insets(5));
        setGraphic(pane);
        LayoutUtils.makeResizable(this, pane, 5);

        setBehavior();
    }

    public void setItems(Collection<? extends CompletionItem> items) {
        itemView.setItems(FXCollections.observableArrayList(items));
    }

    private void setBehavior() {

        focusListener = (v, o, n) -> {

            if (!n) {
                v.removeListener(focusListener);
                close();
            }
        };

        windowListener = (v, o, n) -> {
            v.removeListener(windowListener);
            close();
        };

        handler = e -> {

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

        Nodes.addInputMap(itemView,
                sequence(consume(keyPressed(ENTER), e -> selected()),
                        consume(keyPressed(ESCAPE), e -> close()),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> selected())));

        anchorXProperty().addListener((v, o, n) -> {
            double offset = Screen.getPrimary().getBounds().getWidth() - n.doubleValue() - getPrefWidth() > n.doubleValue() ? getPrefWidth()
                    : -docPopup.getPrefWidth();
            docPopup.setAnchorX(n.doubleValue() + offset);
        });

        anchorYProperty().addListener((v, o, n) -> {
            docPopup.setAnchorY(n.doubleValue());
        });

        itemView.getSelectionModel().selectedItemProperty().addListener((v, o, n) -> {

            if (!isShowing()) {
                return;
            }

            if (n != null) {

                if (docPopup.loadContent(n.getDocRef())) {
                    if (!docPopup.isShowing()) {
                        docPopup.show(this);
                        docPopup.getGraphic().requestFocus();
                    }
                } else {
                    docPopup.hide();
                }
            }
        });
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

    public void close() {
        hide();
    }

    private void selected() {
        hide();
        CompletionItem selection = itemView.getSelectionModel().getSelectedItem();
        if (selection != null) {
            selection.complete();
        }
    }

    @Override
    public void hide() {
        if (isShowing()) {
            getOwnerNode().removeEventFilter(KeyEvent.KEY_PRESSED, handler);
            docPopup.hide();
            super.hide();
        }
    }

    @Override
    public void show(Node ownerNode, double anchorX, double anchorY) {
        if (isShowing()) {
            setAnchorX(anchorX);
            setAnchorY(anchorY);
            itemView.getSelectionModel().clearSelection();
            itemView.getSelectionModel().selectFirst();
        } else {
            ownerNode.focusedProperty().addListener(focusListener);
            ownerNode.getScene().getWindow().xProperty().addListener(windowListener);
            ownerNode.getScene().getWindow().yProperty().addListener(windowListener);
            ownerNode.addEventFilter(KeyEvent.KEY_PRESSED, handler);

            super.show(ownerNode, anchorX, anchorY);

            getGraphic().requestFocus();
            itemView.getSelectionModel().clearSelection();
            itemView.getSelectionModel().selectFirst();
        }
    }
}

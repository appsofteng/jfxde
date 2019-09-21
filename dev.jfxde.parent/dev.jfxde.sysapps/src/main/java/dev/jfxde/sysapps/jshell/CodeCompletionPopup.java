package dev.jfxde.sysapps.jshell;

import static javafx.scene.input.KeyCode.ENTER;
import static javafx.scene.input.KeyCode.ESCAPE;
import static javafx.scene.input.MouseButton.PRIMARY;
import static org.fxmisc.wellbehaved.event.EventPattern.keyPressed;
import static org.fxmisc.wellbehaved.event.EventPattern.mousePressed;
import static org.fxmisc.wellbehaved.event.InputMap.consume;
import static org.fxmisc.wellbehaved.event.InputMap.sequence;

import java.util.List;

import org.fxmisc.wellbehaved.event.Nodes;

import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.Popup;

public class CodeCompletionPopup extends Popup {

    private ListView<CompletionItem> itemView = new ListView<>();
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

    public CodeCompletionPopup(List<? extends CompletionItem> items) {
        // does not work well because it blocks mouse press events outside the popup
        // setAutoHide(true);

        // not working when the list inside the popup has the focus
        setHideOnEscape(false);

        itemView.setItems(FXCollections.observableArrayList(items));
        itemView.setFocusTraversable(false);
        itemView.setPrefHeight(100);
        getContent().add(itemView);
        setInputmap();
    }

    private void setInputmap() {

        Nodes.addInputMap(itemView,
                sequence(consume(keyPressed(ENTER), e -> selected()),
                        consume(keyPressed(ESCAPE), e -> close()),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 1), e -> itemView.setFocusTraversable(true)),
                        consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> selected())));
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

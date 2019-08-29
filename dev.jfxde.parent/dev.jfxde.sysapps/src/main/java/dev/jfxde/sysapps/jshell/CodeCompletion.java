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

import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ListView;
import javafx.stage.Popup;

public class CodeCompletion extends Popup {

	private ListView<String> suggestionView = new ListView<>();
	private String selection;

	public CodeCompletion() {
		suggestionView.setPrefHeight(100);

		// does not work well because it blocks mouse press events outside the popup
		// setAutoHide(true);

		// not working when the list inside the popup has the focus
		// setHideOnEscape(true);

		suggestionView.getSelectionModel().select(0);
		suggestionView.setFocusTraversable(false);
		getContent().add(suggestionView);
		setInputmap();
	}

	public void setSuggestions(List<String> suggestions) {
		suggestionView.setItems(FXCollections.observableArrayList(suggestions));
	}

	private void setInputmap() {

		Nodes.addInputMap(suggestionView,
				sequence(consume(keyPressed(ENTER), e -> seleced()), consume(keyPressed(ESCAPE), e -> seleced()),
						consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 1),
								e -> suggestionView.setFocusTraversable(true)),
						consume(mousePressed(PRIMARY).onlyIf(e -> e.getClickCount() == 2), e -> seleced())));
	}

	public String getSelection() {
		return suggestionView.getSelectionModel().getSelectedItem();
	}

	public void seleced() {
		selection = suggestionView.getSelectionModel().getSelectedItem();
		hide();
	}

	@Override
	public void show(Node ownerNode, double anchorX, double anchorY) {

		ownerNode.focusedProperty().addListener((v, o, n) -> {

			if (!n) {
				hide();
			}
		});

		ownerNode.getScene().getWindow().xProperty().addListener((wv, wo, wn) -> {
			hide();
		});

		ownerNode.getScene().getWindow().yProperty().addListener((wv, wo, wn) -> {
			hide();
		});

		setOnShown(e -> ownerNode.getScene().getWindow().requestFocus());

		super.show(ownerNode, anchorX, anchorY);
	}
}

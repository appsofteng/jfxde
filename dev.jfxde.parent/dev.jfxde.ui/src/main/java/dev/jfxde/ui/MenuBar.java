package dev.jfxde.ui;

import dev.jfxde.api.Fonts;
import dev.jfxde.logic.Sys;
import javafx.animation.TranslateTransition;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.stage.Stage;

public class MenuBar extends BorderPane {

	private HBox activeAppMenuBar = new HBox();
	private Region defaultAppMenuBar = new Region();
	private HBox buttonBox = new HBox();
	private Button restore = new Button(Fonts.Octicons.SCREEN_NORMAL);
	private Button fullScreen = new Button(Fonts.FontAwesome.EXPAND);
	private TranslateTransition menuBarTransition = new TranslateTransition(DesktopEnvironment.SHOW_HIDE_DURATION,
			this);
	private boolean menuBarClicked;

	public MenuBar() {
		addButtons();
		setHandlers();

		HBox.setHgrow(defaultAppMenuBar, Priority.NEVER);
		defaultAppMenuBar.setPrefSize(0, 0);

		activeAppMenuBar.getChildren().add(defaultAppMenuBar);
		activeAppMenuBar.setMaxHeight(USE_PREF_SIZE);

		BorderPane.setAlignment(buttonBox, Pos.CENTER_RIGHT);

		setCenter(activeAppMenuBar);
		setRight(buttonBox);

		setMaxHeight(USE_PREF_SIZE);

		getStyleClass().add("jd-menubar");
	}

	private void addButtons() {
		restore.getStyleClass().addAll("jd-menubar-button", "jd-octicons");
		restore.setTooltip(new Tooltip());
		restore.setDisable(true);

		fullScreen.getStyleClass().addAll("jd-menubar-button", "jd-menubar-button-solid");
		fullScreen.setTooltip(new Tooltip());

		sceneProperty().addListener((v, o, n) -> {

			if (n != null) {
				n.windowProperty().addListener((vv, oo, nn) -> {

					if (nn != null) {
						Stage stage = (Stage)nn;
				        fullScreen.textProperty().bind(Bindings.when(stage.fullScreenProperty()).then(Fonts.FontAwesome.COMPRESS).otherwise(Fonts.FontAwesome.EXPAND));
				        fullScreen.getTooltip().textProperty().bind(Bindings.when(stage.fullScreenProperty()).then(Sys.rm().getTextBinding("restoreScreen")).otherwise(Sys.rm().getTextBinding("fullScreen")));
					}
				});
			}
		});

		buttonBox.getChildren().addAll(restore, fullScreen);
		buttonBox.setMaxWidth(USE_PREF_SIZE);
		buttonBox.setMaxHeight(USE_PREF_SIZE);

		restore.setOnAction(e -> Sys.dm().getActiveDesktop().getActiveWindow().fullRestore());
		fullScreen.setOnAction(
				e -> ((Stage) getScene().getWindow()).setFullScreen(!((Stage) getScene().getWindow()).isFullScreen()));
	}

	private void setHandlers() {
		addEventFilter(MouseEvent.MOUSE_PRESSED, e -> menuBarClicked = true);
		setOnMouseExited(e -> {
			if (!contains(e.getX(), e.getY()) && !menuBarClicked) {
				hide();
			}
		});
	}

	public HBox getButtonBox() {
		return buttonBox;
	}

	public Button getRestore() {
		return restore;
	}

	public boolean isDefaultMenuBar() {
		return activeAppMenuBar.getChildren().get(0) == defaultAppMenuBar;
	}

	public void setActiveAppMenuBar(Region value) {
		if (value == null) {
			value = defaultAppMenuBar;
		} else {
			HBox.setHgrow(value, Priority.ALWAYS);
		}

		activeAppMenuBar.getChildren().set(0, value);
	}

	public void show() {

//      if (menuBarTransition.getStatus() != Status.RUNNING && menuBar.getTranslateY() == 0) {
//          menuBarTransition.setByY(menuBar.getHeight());
//          menuBarTransition.play();
//      }

		menuBarTransition.stop();
		menuBarTransition.setByY(getHeight() - getTranslateY());
		menuBarTransition.play();
	}

	public void hide() {
		menuBarClicked = false;
//       if (menuBar.getTranslateY() > 10) {
		menuBarTransition.stop();
		menuBarTransition.setByY(-getTranslateY());
		menuBarTransition.play();
//       }
	}

}

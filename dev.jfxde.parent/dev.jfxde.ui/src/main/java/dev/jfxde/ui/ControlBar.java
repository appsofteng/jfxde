package dev.jfxde.ui;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.logic.Sys;
import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.Desktop;
import javafx.animation.Animation.Status;
import javafx.animation.TranslateTransition;
import javafx.css.PseudoClass;
import javafx.geometry.Pos;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ControlBar extends VBox {

	private TranslateTransition controlBarTransition = new TranslateTransition(DesktopEnvironment.SHOW_HIDE_DURATION,
			this);
	private static final PseudoClass ACTIVE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("active");

	public ControlBar() {

		Accordion accordion = new Accordion();
		accordion.getPanes().add(createAppPane());
		accordion.getPanes().add(createStartedAppPane());
		accordion.getPanes().add(createDesktopPane());
		accordion.getPanes().add(createSetttingsPane());

		getChildren().add(accordion);

		getStyleClass().add("jd-controlbar");
	}

	void show() {

		if (controlBarTransition.getStatus() != Status.RUNNING && getTranslateX() == 0) {
			controlBarTransition.setByX(getWidth());
			controlBarTransition.play();
		}
	}

	void hide() {

		if (getTranslateX() > 10) {
			controlBarTransition.stop();
			controlBarTransition.setByX(-getTranslateX());
			controlBarTransition.play();
		}
	}

	private TitledPane createAppPane() {

		TextField search = new TextField();
		search.promptTextProperty().bind(Sys.rm().getTextBinding("search"));
		Sys.am().appProviderFilterProperty().bind(search.textProperty());

		ListView<AppProviderDescriptor> appView = new ListView<>();
		appView.setItems(Sys.am().getFilteredAppProviderDescriptors());
		appView.setCellFactory(v -> new AppProviderListCell());

		VBox vbox = new VBox(search, appView);

		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getTextBinding("apps"));
		pane.setContent(vbox);

		return pane;
	}

	private TitledPane createStartedAppPane() {

		TextField search = new TextField();
		search.promptTextProperty().bind(Sys.rm().getTextBinding("search"));
		Sys.am().appFilterProperty().bind(search.textProperty());

		ListView<AppDescriptor> appView = new ListView<>();
		appView.setItems(Sys.am().getFilteredAppDescriptors());
		appView.setCellFactory(v -> new AppListCell());
		Sys.dm().activeWindowProperty().addListener((v, o, n) -> {
			if (n != null) {
				appView.getSelectionModel().select(n.getAppDescriptor());
			} else {
				appView.getSelectionModel().clearSelection();
			}
		});

		VBox vbox = new VBox(search, appView);

		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getTextBinding("started"));
		pane.setContent(vbox);

		return pane;
	}

	private TitledPane createDesktopPane() {

		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getTextBinding("desktops"));

		List<Button> desktopButtons = Sys.dm().getDesktops().stream().map(this::createDesktopButton)
				.collect(Collectors.toList());
		TilePane tilePane = LayoutUtils.createTilePane(desktopButtons);
		tilePane.setAlignment(Pos.CENTER);
		pane.setContent(tilePane);

		return pane;
	}

	private TitledPane createSetttingsPane() {
		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getTextBinding("Settings"));
		ChoiceBox<String> localesChoice = new ChoiceBox<>(Sys.am().getLocales());
		localesChoice.getSelectionModel().select(Sys.sm().getLocal());
		localesChoice.setOnAction(e -> Sys.sm().setLocale(localesChoice.getSelectionModel().getSelectedItem()));
		localesChoice.setTooltip(new Tooltip());
		localesChoice.getTooltip().textProperty().bind(Sys.rm().getTextBinding("changeLanguage"));

		FlowPane flowPane = new FlowPane();
		flowPane.getChildren().add(localesChoice);

		pane.setContent(flowPane);

		return pane;
	}

	private Button createDesktopButton(Desktop desktop) {
		Button button = new Button();
		button.setPrefSize(30, 30);
		button.getStyleClass().add("jd-controlbar-desktops-button");

		desktop.activeProperty().addListener((o, ov, nv) -> {
			button.pseudoClassStateChanged(ACTIVE_PSEUDOCLASS_STATE, nv);
		});

		button.pseudoClassStateChanged(ACTIVE_PSEUDOCLASS_STATE, desktop.isActive());

		button.setOnAction(e -> {
			Sys.dm().setActiveDesktop(desktop);
		});

		return button;
	}

	private static class AppProviderListCell extends ListCell<AppProviderDescriptor> {

		@Override
		protected void updateItem(AppProviderDescriptor item, boolean empty) {
			super.updateItem(item, empty);

			if (item == null || empty) {
				setGraphic(null);
				textProperty().unbind();
				setText(null);
				setOnMouseClicked(null);
				setContextMenu(null);
			} else {
				setGraphic(item.getSmallIcon());
				textProperty().bind(item.nameProperty());
				setOnMouseClicked(e -> {

					if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 2)
						Sys.am().start(item);
				});

				ContextMenu contextMenu = new ContextMenu();
				MenuItem createShortcut = new MenuItem();
				createShortcut.textProperty().bind(Sys.rm().getTextBinding("createShortcut"));
				createShortcut.setOnAction(
						e -> Sys.dm().addShortcut(item));

				contextMenu.getItems().add(createShortcut);

				setContextMenu(contextMenu);

			}
		}
	}

	private static class AppListCell extends ListCell<AppDescriptor> {

		@Override
		protected void updateItem(AppDescriptor item, boolean empty) {
			super.updateItem(item, empty);

			if (item == null || empty) {
				setGraphic(null);
				textProperty().unbind();
				setText(null);
				setOnMouseClicked(null);
				setContextMenu(null);
			} else {
				setGraphic(item.getAppProviderDescriptor().getSmallIcon());
				textProperty().bind(item.displayProperty());
				setOnMouseClicked(e -> {

					if (e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1)
						Sys.am().activate(item);
				});
			}
		}
	}
}

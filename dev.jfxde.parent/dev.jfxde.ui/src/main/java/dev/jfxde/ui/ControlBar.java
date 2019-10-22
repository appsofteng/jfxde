package dev.jfxde.ui;

import java.util.List;
import java.util.stream.Collectors;

import dev.jfxde.jfxext.util.LayoutUtils;
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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

public class ControlBar extends VBox {

	private TranslateTransition controlBarTransition = new TranslateTransition(DesktopEnvironment.SHOW_HIDE_DURATION,
			this);
	private static final PseudoClass ACTIVE_PSEUDOCLASS_STATE = PseudoClass.getPseudoClass("active");

	private TitledPane appPane;
	private TitledPane startedAppPane;

	public ControlBar() {

		Accordion accordion = new Accordion();
		accordion.getPanes().add(appPane = createAppPane());
		accordion.getPanes().add(startedAppPane = createStartedAppPane());
		accordion.getPanes().add(createDesktopPane());

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

	void bindDesktop(DesktopPane desktopPane) {
	    appPane.disableProperty().unbind();
	    startedAppPane.disableProperty().unbind();

	    appPane.disableProperty().bind(desktopPane.frozenProperty());
	    startedAppPane.disableProperty().bind(desktopPane.frozenProperty());
	}

	private TitledPane createAppPane() {

		TextField search = new TextField();
		search.promptTextProperty().bind(Sys.rm().getStringBinding("search"));
		Sys.am().appProviderFilterProperty().bind(search.textProperty());

		ListView<AppProviderDescriptor> appView = new ListView<>();
		appView.setItems(Sys.am().getFilteredAppProviderDescriptors());
		appView.setCellFactory(v -> new AppProviderListCell());

		VBox vbox = new VBox(search, appView);

		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getStringBinding("apps"));
		pane.setContent(vbox);

		return pane;
	}

	private TitledPane createStartedAppPane() {

		TextField search = new TextField();
		search.promptTextProperty().bind(Sys.rm().getStringBinding("search"));
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
		pane.textProperty().bind(Sys.rm().getStringBinding("started"));
		pane.setContent(vbox);

		return pane;
	}

	private TitledPane createDesktopPane() {

		TitledPane pane = new TitledPane();
		pane.textProperty().bind(Sys.rm().getStringBinding("desktops"));

		List<Button> desktopButtons = Sys.dm().getDesktops().stream().map(this::createDesktopButton)
				.collect(Collectors.toList());
		TilePane tilePane = LayoutUtils.createTilePane(desktopButtons);
		tilePane.setAlignment(Pos.CENTER);
		pane.setContent(tilePane);

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
				createShortcut.textProperty().bind(Sys.rm().getStringBinding("createShortcut"));
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

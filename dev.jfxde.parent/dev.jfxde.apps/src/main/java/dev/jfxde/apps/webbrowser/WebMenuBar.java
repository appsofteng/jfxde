package dev.jfxde.apps.webbrowser;

import java.util.Set;

import org.controlsfx.control.textfield.AutoCompletionBinding;
import org.controlsfx.control.textfield.TextFields;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.Fonts;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.TilePane;

public class WebMenuBar extends BorderPane {

	private AppContext context;
	private WebPageView webPageView;
	private Button back = new Button(Fonts.FontAwesome.ARROW_LEFT);
	private Button forward = new Button(Fonts.FontAwesome.ARROW_RIGHT);
	private Button reload = new Button(Fonts.FontAwesome.REDO);
	private TextField urlField = new TextField();
	private Set<String> locations;
	private AutoCompletionBinding<String> urlFieldBinding;

	public WebMenuBar(AppContext context, WebPageView webPageView, Set<String> locations) {
		this.context = context;
		this.webPageView = webPageView;
		this.locations = locations;

		setMaxHeight(USE_PREF_SIZE);
		initControls();
		setListeners();
	}

	private void initControls() {
		back.getStyleClass().addAll("jd-menubar-button-solid");
		back.setTooltip(new Tooltip());
		back.getTooltip().textProperty().bind(context.rc().getTextBinding("back"));
		back.setOnAction(e -> webPageView.back());
		back.disableProperty().bind(webPageView.backDisableProperty());
		back.setFocusTraversable(false);

		forward.getStyleClass().addAll("jd-menubar-button-solid");
		forward.setTooltip(new Tooltip());
		forward.getTooltip().textProperty().bind(context.rc().getTextBinding("forward"));
		forward.setOnAction(e -> webPageView.forward());
		forward.disableProperty().bind(webPageView.forwardDisableProperty());
		forward.setFocusTraversable(false);

		reload.getStyleClass().addAll("jd-menubar-button-solid");
		reload.setTooltip(new Tooltip());
		reload.textProperty().bind(Bindings.when(webPageView.runningProperty()).then(Fonts.FontAwesome.TIMES)
				.otherwise(Fonts.FontAwesome.REDO));
		reload.getTooltip().textProperty().bind(Bindings.when(webPageView.runningProperty())
				.then(context.rc().getTextBinding("stop")).otherwise(context.rc().getTextBinding("reload")));
		reload.setFocusTraversable(false);

		TilePane buttonPane = new TilePane();
		buttonPane.getChildren().addAll(back, forward, reload);
		buttonPane.setMaxWidth(USE_PREF_SIZE);
		buttonPane.setMaxHeight(USE_PREF_SIZE);
		BorderPane.setAlignment(buttonPane, Pos.CENTER_LEFT);

		Platform.runLater(this::bindAutoCompletion);

		setLeft(buttonPane);
		setCenter(urlField);
	}

	private void setListeners() {

		webPageView.locationProperty().addListener((v, o, n) -> {

			if (n != null && !n.equals(urlField.getText())) {
				urlField.setText(n);
			}
		});

		reload.setOnAction(e -> {
			if (webPageView.isRunning()) {
				webPageView.stop();
			} else {
				webPageView.reload();
			}
		});

		urlField.setOnAction(e -> onLocationSelected());
	}

	private void bindAutoCompletion() {
		urlFieldBinding = TextFields.bindAutoCompletion(urlField, locations);
		urlFieldBinding.setOnAutoCompleted(e -> webPageView.load(urlField.getText()));
	}

	private void onLocationSelected() {

		locations.add(urlField.getText());
		if (urlFieldBinding != null) {
			urlFieldBinding.dispose();
		}

		bindAutoCompletion();

		webPageView.load(urlField.getText());
	}
}

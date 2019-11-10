package dev.jfxde.apps.webbrowser;

import java.util.Objects;

import dev.jfxde.api.AppContext;
import dev.jfxde.jfx.scene.web.JSUtils;
import dev.jfxde.jfx.util.FXResourceBundle;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.concurrent.Worker.State;
import javafx.geometry.Bounds;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;

public class WebPageView extends StackPane {

//	private static final String FIREFOX_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:67.0) Gecko/20100101 Firefox/67.0";

	private AppContext context;
	private WebView webView;
	private WebEngine engine;
	private StringProperty location = new SimpleStringProperty();
	private StringProperty title = new SimpleStringProperty();
	private BooleanProperty backDisable = new SimpleBooleanProperty(true);
	private BooleanProperty forwardDisable = new SimpleBooleanProperty(true);
	private BooleanProperty running = new SimpleBooleanProperty(false);

	private SearchEngine searchEngine;
	private LocationUrl locationUrl;
	private Tooltip linkPreview = new Tooltip();

	private StringProperty linkInNewTab = new SimpleStringProperty();

	public WebPageView(AppContext context, String url) {
		this.context = context;
		// WebView must run on the FX application thread.
		Platform.runLater(() -> {
			webView = new WebView();
			webView.setContextMenuEnabled(false);
			engine = webView.getEngine();
			engine.setUserDataDirectory(context.fc().getAppDataDir("userData").toFile());
			// engine.setUserAgent(FIREFOX_USER_AGENT);
			setListeners();
			getChildren().add(webView);
			load(url);
		});
	}

	private void setListeners() {
		FXResourceBundle.getBundle().put(title, "new");
		backDisable.bind(Bindings.equal(engine.getHistory().currentIndexProperty(), 0));
		forwardDisable.bind(Bindings.isEmpty(engine.getHistory().getEntries()).or(engine.getHistory()
				.currentIndexProperty().isEqualTo(Bindings.size(engine.getHistory().getEntries()).subtract(1))));
		running.bind(engine.getLoadWorker().runningProperty());

		engine.titleProperty().addListener((v, o, n) -> {
			if (n != null) {
				title.unbind();
				title.set(n);
			}
		});

		engine.locationProperty().addListener((v, o, n) -> {

			if (n != null) {
				location.set(n);
			}
		});

		engine.getLoadWorker().stateProperty().addListener((v, o, n) -> {

			if (n == State.FAILED) {

				throw new RuntimeException(engine.getLoadWorker().getException());
			}
		});

		webView.setOnMouseExited(e -> {
			linkPreview.hide();
		});

		webView.addEventFilter(MouseEvent.MOUSE_MOVED, e -> {

			JSObject jsobject = JSUtils.getElementFromPoint(engine, e.getX(), e.getY());
			JSObject linkJsobject = JSUtils.getJSObject(jsobject, "a");

			if (linkJsobject != null) {
				String url = (String) linkJsobject.getMember("href");
				Bounds bounds = webView.getBoundsInLocal();
				Bounds screenBounds = webView.localToScreen(bounds);

				linkPreview.setText(url);
				// linkPreview.getHeight() returns varying height
				linkPreview.show(webView, screenBounds.getMinX(),
						screenBounds.getMinY() + screenBounds.getHeight() - 21);
			} else {
				linkPreview.hide();
			}
		});

		WebContextMenuBuilder.register(this);
	}

	public WebView getWebView() {
		return webView;
	}

	public ReadOnlyStringProperty linkInNewTabPropery() {
		return linkInNewTab;
	}

	public void setLinkInNewTab(String value) {
		linkInNewTab.set(null);
		linkInNewTab.set(value);
	}

	public SearchEngine getSearchEngine() {
		return searchEngine;
	}

	public ReadOnlyStringProperty titleProperty() {
		return title;
	}

	public ReadOnlyBooleanProperty runningProperty() {
		return running;
	}

	public boolean isRunning() {
		return running.get();
	}

	public ReadOnlyStringProperty locationProperty() {
		return location;
	}

	public ReadOnlyBooleanProperty backDisableProperty() {
		return backDisable;
	}

	public ReadOnlyBooleanProperty forwardDisableProperty() {
		return forwardDisable;
	}

	public void setSearchEngine(SearchEngine searchEngine) {
		this.searchEngine = searchEngine;
	}

	public void load(String url) {

		if (!Objects.requireNonNullElse(url, "").isEmpty()) {

			locationUrl = new LocationUrl(url);
			String loadUrl = locationUrl.getFormattedUrl(searchEngine);
			location.set(loadUrl);

			engine.load(loadUrl);
		}
	}

	public void search(String query) {
		String searchUrl = searchEngine.getQueryUrl(query);
		location.set(searchUrl);
		engine.load(searchUrl);
	}

	public void back() {
		if (engine.getHistory().getCurrentIndex() > 0) {
			engine.getHistory().go(-1);
		}
	}

	public void forward() {
		if (engine.getHistory().getCurrentIndex() + 1 < engine.getHistory().getEntries().size()) {
			engine.getHistory().go(1);
		}
	}

	public void reload() {
		engine.reload();
	}

	public void close() {
		engine.getLoadWorker().cancel();
		engine.load("");
	}

	public void stop() {
		engine.getLoadWorker().cancel();
	}
}

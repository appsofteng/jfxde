package dev.jfxde.apps.webbrowser;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppRequest;
import dev.jfxde.api.ui.Fonts;
import javafx.application.Platform;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TabPane.TabClosingPolicy;
import javafx.scene.control.TabPane.TabDragPolicy;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.Pane;

public class WebBrowserContent extends Pane {

    private AppContext context;
    private TabPane tabPane;
    private Button newButton = new Button(Fonts.Unicode.FULLWIDTH_PLUS_SIGN);

    public WebBrowserContent(AppContext context) {
        this.context = context;
        getStylesheets().add(context.rc().getCss("browser"));

        tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabClosingPolicy.ALL_TABS);
        tabPane.setTabDragPolicy(TabDragPolicy.REORDER);
        tabPane.setTabMinWidth(50);
        tabPane.setTabMaxWidth(200);

        AppRequest request = context.getRequest();
        if (request != null) {
            addBrowserTab(request.getResource().getUri().toString());
        } else {
            addBrowserTab("");
        }

        newButton.setFocusTraversable(false);
        newButton.setContentDisplay(ContentDisplay.TEXT_ONLY);
        newButton.getStyleClass().add("jd-new-tab-btn");
        newButton.setOnAction(e -> addBrowserTab(""));

        getChildren().addAll(tabPane, newButton);
    }

    public void stop() {
        Platform.runLater(() -> tabPane.getTabs().forEach(t -> ((WebBrowser) t.getContent()).close()));
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        layoutInArea(tabPane, 0, 0, getWidth(), getHeight(), 0, new Insets(0), HPos.CENTER, VPos.CENTER);
        positionInArea(newButton, 5, 8, newButton.getWidth(), newButton.getHeight(), 0, HPos.CENTER, VPos.CENTER);
    }

    private void addBrowserTab(String url) {
        Tab tab = new Tab();
        WebBrowser browser = new WebBrowser(context, url, Controller.dm().getLocations());
        browser.linkInNewTabPropery().addListener((v, o, n) -> {
        	if (n != null) {
        		addBrowserTab(n);
        	}
        });

        tab.textProperty().bind(browser.titleProperty());
        tab.setTooltip(new Tooltip());
        tab.getTooltip().textProperty().bind(browser.titleProperty());
        tab.setContent(browser);
        tab.setOnClosed(e -> browser.close());

        tabPane.getTabs().add(tab);
        tabPane.getSelectionModel().select(tab);
    }
}

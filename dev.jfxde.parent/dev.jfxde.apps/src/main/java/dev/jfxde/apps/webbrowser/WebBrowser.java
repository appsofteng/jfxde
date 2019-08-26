package dev.jfxde.apps.webbrowser;

import java.util.Set;

import dev.jfxde.api.AppContext;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.scene.layout.BorderPane;

public class WebBrowser extends BorderPane {

    private WebMenuBar webMenuBar;
    private WebPageView webPageView;

    public WebBrowser(AppContext context, String url, Set<String> locations) {
        webPageView = new WebPageView(context, url);
        webPageView.setSearchEngine(Controller.dm().getDefaultSearchEngine());
        webMenuBar = new WebMenuBar(context, webPageView, locations);

        setTop(webMenuBar);
        setCenter(webPageView);
    }

    public ReadOnlyStringProperty titleProperty() {
        return webPageView.titleProperty();
    }

    public ReadOnlyStringProperty linkInNewTabPropery() {
    	return webPageView.linkInNewTabPropery();
    }

    public void close() {
        webPageView.close();
    }
}

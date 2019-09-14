module dev.jfxde.apps {

    requires java.desktop;
	requires jdk.jsobject;
    requires jdk.net;
	requires javafx.controls;
	requires dev.jfxde.api;
	requires javafx.web;
	requires org.controlsfx.controls;
	requires javafx.graphics;
	requires javafx.base;

	provides dev.jfxde.api.App with dev.jfxde.apps.webbrowser.WebBrowserApp;

	opens  dev.jfxde.apps.webbrowser.bundles;
	opens  dev.jfxde.apps.webbrowser.css;
}
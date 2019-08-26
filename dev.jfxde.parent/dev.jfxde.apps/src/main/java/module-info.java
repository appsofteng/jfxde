module dev.jfxde.apps {

	requires jdk.jsobject;
	requires javafx.controls;
	requires dev.jfxde.api;
	requires javafx.web;
	requires org.controlsfx.controls;
	requires javafx.graphics;
	requires java.desktop;
	requires jdk.net;
	requires javafx.base;

	provides dev.jfxde.api.App with dev.jfxde.apps.webbrowser.WebBrowserApp;

	opens  dev.jfxde.apps.webbrowser.bundles;
	opens  dev.jfxde.apps.webbrowser.css;
}
module dev.jfxde.apps {

    requires java.desktop;
    requires jdk.jsobject;
    requires jdk.net;
    requires javafx.controls;
    requires javafx.web;
    requires org.controlsfx.controls;
    requires javafx.graphics;
    requires javafx.base;

    requires dev.jfxde.api;
    requires dev.jfxde.fonts;
    requires dev.jfxde.jfxext;

    provides dev.jfxde.api.App with dev.jfxde.apps.webbrowser.WebBrowserApp;

    opens dev.jfxde.apps.webbrowser.bundles;
    opens dev.jfxde.apps.webbrowser.css;
}
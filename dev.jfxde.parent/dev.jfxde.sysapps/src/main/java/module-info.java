module dev.jfxde.sysapps {
    requires java.management;
    requires java.logging;
    requires jdk.jshell;
    requires javafx.controls;
    requires javafx.graphics;
    requires javafx.base;
    requires javafx.web;
    requires richtextfx;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;
    requires org.controlsfx.controls;

    requires dev.jfxde.api;
    requires dev.jfxde.logic;
    requires dev.jfxde.ui;
    requires dev.jfxde.jfxext;

    provides dev.jfxde.api.App with dev.jfxde.sysapps.appmanager.AppManagerApp,
            dev.jfxde.sysapps.console.ConsoleApp, dev.jfxde.sysapps.exceptionlog.ExceptionLogApp,
            dev.jfxde.sysapps.jvmmonitor.JvmMonitorApp, dev.jfxde.sysapps.jshell.JShellApp, dev.jfxde.sysapps.settings.SettingsApp;

    opens dev.jfxde.sysapps.appmanager.bundles;
    opens dev.jfxde.sysapps.console.css;
    opens dev.jfxde.sysapps.jvmmonitor.bundles;
    opens dev.jfxde.sysapps.jshell.bundles;
    opens dev.jfxde.sysapps.jshell.css;
}

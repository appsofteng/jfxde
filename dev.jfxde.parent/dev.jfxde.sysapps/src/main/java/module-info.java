module dev.jfxde.sysapps {
	requires java.management;
	requires javafx.controls;
	requires dev.jfxde.api;
	requires dev.jfxde.logic;
	requires dev.jfxde.ui;
	requires javafx.graphics;
	requires javafx.base;
	requires richtextfx;
	requires flowless;
	requires reactfx;
	requires org.controlsfx.controls;

	provides dev.jfxde.api.App with dev.jfxde.sysapps.appmanager.AppManagerApp,
			dev.jfxde.sysapps.console.ConsoleApp, dev.jfxde.sysapps.exceptionlog.ExceptionLogApp,
			dev.jfxde.sysapps.jvmmonitor.JvmMonitorApp;

	opens  dev.jfxde.sysapps.appmanager.bundles;
	opens  dev.jfxde.sysapps.console.bundles;
	opens  dev.jfxde.sysapps.console.css;
	opens  dev.jfxde.sysapps.exceptionlog.bundles;
	opens  dev.jfxde.sysapps.jvmmonitor.bundles;
}

module dev.jfxde.jfxext {

    requires java.logging;
    requires transitive java.prefs;

    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.graphics;
    requires transitive javafx.web;
    requires transitive jdk.jsobject;

    requires org.controlsfx.controls;
    requires transitive richtextfx;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;
    requires undofx;

    requires com.github.javaparser.core;

    exports dev.jfxde.jfxext.control to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfxext.richtextfx.features to dev.jfxde.sysapps;
    exports dev.jfxde.jfxext.richtextfx to dev.jfxde.sysapps, dev.jfxde.logic;
    exports dev.jfxde.jfxext.util to dev.jfxde.sysapps, dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.apps;
    exports dev.jfxde.jfxext.util.prefs to dev.jfxde.logic;

    opens dev.jfxde.jfxext.util.prefs to java.prefs;
    opens dev.jfxde.jfxext.control to javafx.graphics;
}
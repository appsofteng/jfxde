module dev.jfxde.jfxext {

    requires java.logging;
    requires transitive java.prefs;
    requires java.desktop;

    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.graphics;
    requires transitive javafx.web;
    requires transitive jdk.jsobject;
    requires javafx.swing;

    requires org.controlsfx.controls;
    requires transitive richtextfx;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;
    requires undofx;

    requires com.github.javaparser.core;


    exports dev.jfxde.fxmisc.richtext to dev.jfxde.sysapps, dev.jfxde.logic;
    exports dev.jfxde.fxmisc.richtext.extensions to dev.jfxde.sysapps;

    exports dev.jfxde.j.nio.file to dev.jfxde.logic, dev.jfxde.sysapps;
    exports dev.jfxde.j.util to dev.jfxde.logic, dev.jfxde.sysapps;
    exports dev.jfxde.j.util.prefs to dev.jfxde.logic;

    exports dev.jfxde.jfx.animation to dev.jfxde.ui;
    exports dev.jfxde.jfx.application to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfx.concurrent to dev.jfxde.sysapps, dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.apps;
    exports dev.jfxde.jfx.embed.swing to dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfx.scene.control to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps, dev.jfxde.apps;
    exports dev.jfxde.jfx.scene.control.cell to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfx.scene.layout to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfx.scene.web to dev.jfxde.apps;
    exports dev.jfxde.jfx.util to dev.jfxde.sysapps, dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.apps;
    exports dev.jfxde.jfx.util.converter to dev.jfxde.sysapps, dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.apps;

    exports dev.jfxde.jx.tools to dev.jfxde.sysapps;

    opens dev.jfxde.j.util.prefs to java.prefs;
    opens dev.jfxde.jfx.scene.control to javafx.graphics;
}
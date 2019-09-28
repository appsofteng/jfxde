module dev.jfxde.jfxext {

    requires java.logging;

    requires javafx.base;
    requires transitive javafx.controls;
    requires javafx.graphics;
    requires transitive javafx.web;
    requires transitive jdk.jsobject;

    requires transitive richtextfx;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;

    requires com.github.javaparser.core;

    exports dev.jfxde.jfxext.control to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfxext.control.editor to dev.jfxde.sysapps;
    exports dev.jfxde.jfxext.richtextfx to dev.jfxde.sysapps, dev.jfxde.logic;
    exports dev.jfxde.jfxext.util to dev.jfxde.sysapps, dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.apps;

    opens dev.jfxde.jfxext.control to javafx.graphics;
}
module dev.jfxde.jfxext {

    requires java.logging;

    requires transitive javafx.controls;

    requires transitive richtextfx;
    requires flowless;
    requires reactfx;
    requires wellbehavedfx;
    requires javafx.base;

    exports dev.jfxde.jfxext.control to dev.jfxde.logic, dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.jfxext.richtextfx to dev.jfxde.sysapps, dev.jfxde.logic;
    exports dev.jfxde.jfxext.util to dev.jfxde.logic;

    opens dev.jfxde.jfxext.control to javafx.graphics;
}
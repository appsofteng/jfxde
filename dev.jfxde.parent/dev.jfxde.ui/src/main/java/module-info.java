module dev.jfxde.ui {
    requires javafx.graphics;
    requires transitive javafx.controls;
    requires transitive javafx.base;
    requires dev.jfxde.api;
    requires transitive dev.jfxde.logic;

    exports dev.jfxde.ui to dev.jfxde.sysapps;

    opens dev.jfxde.ui to javafx.graphics;
    opens  dev.jfxde.ui.bundles to  dev.jfxde.logic;
    opens  dev.jfxde.ui.css to  dev.jfxde.logic;
}

module dev.jfxde.api {

    requires transitive javafx.base;
    requires transitive javafx.graphics;

    exports dev.jfxde.api;
    exports dev.jfxde.api.ui to dev.jfxde.ui, dev.jfxde.sysapps, dev.jfxde.apps, dev.jfxde.demos;

}
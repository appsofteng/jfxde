module dev.jfxde.logic {
    requires transitive javafx.controls;
    requires transitive dev.jfxde.api;
    requires transitive dev.jfxde.data;
    requires javafx.graphics;
    requires transitive java.logging;
    requires javafx.base;
    requires org.eclipse.yasson;
    requires transitive dev.jfxde.jfxext;
    requires java.json.bind;

    exports dev.jfxde.logic to  dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.logic.data to  dev.jfxde.ui, dev.jfxde.sysapps;

    opens dev.jfxde.logic.data to javafx.base;
    opens dev.jfxde.logic to java.base;

    uses dev.jfxde.api.App;

}

module dev.jfxde.logic {
    requires transitive javafx.controls;
    requires dev.jfxde.api;
    requires transitive dev.jfxde.data;
    requires javafx.graphics;
    requires java.persistence;
    requires org.hibernate.orm.core;
    requires net.bytebuddy;
    requires java.xml.bind;
    requires java.sql;
    requires transitive java.logging;
    requires com.h2database;
    requires javafx.base;
    requires transitive dev.jfxde.jfxext;

    exports dev.jfxde.logic to  dev.jfxde.ui, dev.jfxde.sysapps;
    exports dev.jfxde.logic.data to  dev.jfxde.ui, dev.jfxde.sysapps;

    opens dev.jfxde.logic.data to javafx.base;

    uses dev.jfxde.api.App;

}

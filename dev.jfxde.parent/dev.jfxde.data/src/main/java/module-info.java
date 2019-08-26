module dev.jfxde.data {
	requires transitive javafx.base;
	requires transitive dev.jfxde.api;
	requires javafx.graphics;
	requires javafx.controls;
	requires transitive java.persistence;
	requires org.hibernate.orm.core;

	exports dev.jfxde.data.dao to  dev.jfxde.logic;
	exports dev.jfxde.data.entity to dev.jfxde.logic, org.hibernate.orm.core;

	opens dev.jfxde.data.entity to org.hibernate.orm.core;
}
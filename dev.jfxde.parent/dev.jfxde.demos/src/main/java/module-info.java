module dev.jfxde.demos {
	requires javafx.controls;
	requires javafx.fxml;
	requires dev.jfxde.api;

	provides dev.jfxde.api.App with dev.jfxde.demos.hello.HelloApp, dev.jfxde.demos.hellofxml.HelloFxmlApp;

	opens dev.jfxde.demos.hello.bundles;
	opens dev.jfxde.demos.hello.icons;
    opens dev.jfxde.demos.hellofxml.bundles;
    opens dev.jfxde.demos.hellofxml.icons;
}
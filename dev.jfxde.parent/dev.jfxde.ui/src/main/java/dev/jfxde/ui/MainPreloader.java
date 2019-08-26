package dev.jfxde.ui;

import javafx.application.Preloader;
import javafx.scene.Scene;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class MainPreloader extends Preloader {

	private ProgressBar bar;
	private Stage stage;

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;

		bar = new ProgressBar();
		BorderPane p = new BorderPane();
		p.setStyle("-fx-background-color: rgba(1.0, 1.0, 1.0, 1.0);");
		p.setCenter(bar);
		Scene scene =  new Scene(p, 300, 150);
		stage.setScene(scene);

		stage.initStyle(StageStyle.UNDECORATED);
		stage.show();
	}

	@Override
	public void handleApplicationNotification(PreloaderNotification notif) {

		if (notif instanceof ProgressNotification) {
			ProgressNotification pn = (ProgressNotification)notif;
			bar.setProgress(pn.getProgress());
		}
	}

	@Override
	public void handleStateChangeNotification(StateChangeNotification evt) {
		if (evt.getType() == StateChangeNotification.Type.BEFORE_START) {
			stage.hide();
		}
	}
}

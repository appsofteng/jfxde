package dev.jfxde.apps.webbrowser;

public class Controller {

	 private static final Controller INSTANCE = new Controller();
	 private DataController dataManager = new DataController();

	 public static DataController dm() {
		 return INSTANCE.dataManager;
	 }
}

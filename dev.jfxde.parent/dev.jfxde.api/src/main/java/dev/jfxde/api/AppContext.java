package dev.jfxde.api;

public interface AppContext {

    AppRequest getRequest();
	FileController fc();
    ResourceController rc();
    TaskController tc();
    AppController ac();
}

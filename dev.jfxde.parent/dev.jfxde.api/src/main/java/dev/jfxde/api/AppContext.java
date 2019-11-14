package dev.jfxde.api;

public interface AppContext {

    AppRequest getRequest();
    FileController fc();
    DataController dc();
    ResourceController rc();
    TaskController tc();
    AppController ac();
}

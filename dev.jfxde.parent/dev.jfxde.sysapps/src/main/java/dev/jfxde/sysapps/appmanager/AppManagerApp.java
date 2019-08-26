package dev.jfxde.sysapps.appmanager;



import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="org.jb.AppManager", name="App Manager", version="1.0.0", altText="AM", scope=AppScope.SINGLETON, vendor="JB")
public class AppManagerApp implements SystemApp {

    @Override
    public Node start(AppContext context) {
    	return new AppManagerContent(this, context);
    }
 }

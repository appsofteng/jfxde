package dev.jfxde.sysapps.settings;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="dev.jfxde.settings", name="Settings", version="1.0.0", altText="S", scope=AppScope.SINGLETON, vendor="JFXDE")
public class SettingsApp implements SystemApp {

    @Override
    public Node start(AppContext context) throws Exception {
        return new SettingsContent(context);
    }

}

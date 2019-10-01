package dev.jfxde.sysapps.preferences;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn="dev.jfxde.preferences", name="Preferences", version="1.0.0", altText="P", scope=AppScope.SINGLETON, vendor="JFXDE")
public class PreferencesApp implements SystemApp {

    @Override
    public Node start(AppContext context) throws Exception {
        return new PreferencesContent(context);
    }

}

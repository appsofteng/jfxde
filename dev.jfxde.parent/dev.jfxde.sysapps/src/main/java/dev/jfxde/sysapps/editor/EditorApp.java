package dev.jfxde.sysapps.editor;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;

@AppManifest(fqn = "dev.jfxde.editor", name = "Editor", version = "1.0.0", altText = "E", vendor = "JFXDE")
public class EditorApp implements SystemApp {

    @Override
    public Node start(AppContext context) throws Exception {
        return new EditorContent(context);
    }

}

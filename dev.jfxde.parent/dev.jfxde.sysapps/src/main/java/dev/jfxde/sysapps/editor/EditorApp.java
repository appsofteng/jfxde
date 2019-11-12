package dev.jfxde.sysapps.editor;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.logic.SystemApp;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.scene.Node;

@AppManifest(fqn = "dev.jfxde.editor", name = "Editor", version = "1.0.0", altText = "E", vendor = "JFXDE")
public class EditorApp implements SystemApp {

    private EditorContent content = new EditorContent();

    @Override
    public Node start(AppContext context) throws Exception {
        return content.init(context);
    }

    @Override
    public ReadOnlyBooleanProperty stoppableProperty() {
        return content.stoppableProperty();
    }

}

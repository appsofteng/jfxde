package dev.jfxde.apps.editor;

import java.io.FilePermission;
import java.util.PropertyPermission;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.PermissionEntry;
import javafx.scene.Node;

@AppManifest(fqn = "dev.jfxde.editor", name = "Editor", version = "1.0.0", altText = "E", vendor = "JFXDE")
@PermissionEntry(type = FilePermission.class, target = "<<ALL FILES>>", actions = "read,write,execute,delete,readlink ")
@PermissionEntry(type = RuntimePermission.class, target = "accessDeclaredMembers")
@PermissionEntry(type = PropertyPermission.class, target = "user.dir", actions = "read")
public class EditorApp implements App {

    @Override
    public Node start(AppContext context) throws Exception {
        return new EditorContent(context);
    }

}

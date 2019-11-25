package dev.jfxde.apps.webbrowser;

import java.io.FilePermission;
import java.net.NetPermission;
import java.net.SocketPermission;
import java.security.AllPermission;
import java.util.PropertyPermission;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.PermissionEntry;
import javafx.scene.Node;
import javafx.util.FXPermission;

@AppManifest(fqn = "dev.jfxde.webbrowser", name = "Web Browser", version = "1.0.0", altText = "WB", vendor = "JFXDE", uriSchemes = {"http", "https"})
@PermissionEntry(type = SocketPermission.class, target = "*", actions = "connect,accept,listen")
@PermissionEntry(type = NetPermission.class, target = "*")
@PermissionEntry(type = PropertyPermission.class, target = "*", actions = "read,write")
@PermissionEntry(type = FilePermission.class, target = "<<ALL FILES>>", actions = "read")
@PermissionEntry(type = FXPermission.class, target = "*")
public class WebBrowserApp implements App {

    private WebBrowserContent content;

    @Override
    public Node start(AppContext context) {
        content = new WebBrowserContent(context);
        return content;
    }

    @Override
    public void stop() {
       content.stop();
    }
}

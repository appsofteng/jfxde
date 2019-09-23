package dev.jfxde.sysapps.jshell;

import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.logic.SystemApp;
import javafx.scene.Node;
import jdk.jshell.tool.JavaShellToolBuilder;

@AppManifest(fqn = "dev.jfxde.jshell", name = "JShell", version = "1.0.0", altText = "JS", vendor = "JFXDE")
public class JShellApp implements SystemApp {

    private JShellContent content;

    @Override
    public Node start(AppContext context) throws Exception {

        JavaShellToolBuilder
                .builder()
                .start();

        content = new JShellContent();
        return content;
    }
}

package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;

public class Env {

    private String addModules;
    private String addExports;
    private String classPath;
    private String modulePath;

    public String[] getOptions() {

        List<String> options = new ArrayList<>();

        if (addModules != null) {
            options.add("--add-modules " + addModules);
        }

        if (addExports != null) {
            options.add("--add-exports " + addExports);
        }

        if (classPath != null) {
            options.add("--class-path " + classPath);
        }

        if (modulePath != null) {
            options.add("--module-path " + modulePath);
        }

        return options.toArray(new String[] {});
    }

}

package dev.jfxde.sysapps.jshell;

public class ExportItem {

    private String sourceModule;
    private String packageName;
    private String targetModule;


    @Override
    public String toString() {
        return sourceModule + "/" + packageName + "=" + targetModule;
    }
}

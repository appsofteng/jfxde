package dev.jfxde.sysapps.jshell;

public class ExportItem {

    private String sourceModule;
    private String packageName;
    private String targetModule;

    public ExportItem() {
    }

    public ExportItem(String sourceModule, String packageName, String targetModule) {

        this.sourceModule = sourceModule;
        this.packageName = packageName;
        this.targetModule = targetModule;
    }

    public String getSourceModule() {
        return sourceModule;
    }

    public void setSourceModule(String sourceModule) {
        this.sourceModule = sourceModule;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getTargetModule() {
        return targetModule;
    }

    public void setTargetModule(String targetModule) {
        this.targetModule = targetModule;
    }

    @Override
    public String toString() {
        return sourceModule + "/" + packageName + "=" + targetModule;
    }
}

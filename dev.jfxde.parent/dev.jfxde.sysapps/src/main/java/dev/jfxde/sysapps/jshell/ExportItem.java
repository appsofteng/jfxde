package dev.jfxde.sysapps.jshell;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ExportItem {

    private String sourceModule;
    private String packageName;
    private List<String> targetModules = new ArrayList<>();

    public ExportItem() {
    }

    public ExportItem(String sourceModule, String packageName, String targetModule) {
        this.sourceModule = sourceModule;
        this.packageName = packageName;
        targetModules.add(targetModule);
    }

    public ExportItem(String sourceModule, String packageName, List<String> targetModules) {

        this.sourceModule = sourceModule;
        this.packageName = packageName;
        this.targetModules = targetModules;
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

    public List<String> getTargetModules() {
        return targetModules;
    }

    public void setTargetModules(List<String> targetModule) {
        this.targetModules = targetModule;
    }

    public String getTargetModuleLabel() {
        return targetModules.stream().collect(Collectors.joining(","));
    }

    @Override
    public String toString() {
        return sourceModule + "/" + packageName + "=" + getTargetModuleLabel();
    }
}

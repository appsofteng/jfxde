package dev.jfxde.logic.context;

import java.lang.reflect.Type;
import java.nio.file.Path;

import dev.jfxde.api.DataController;
import dev.jfxde.logic.JsonUtils;
import dev.jfxde.logic.data.AppDescriptor;

public class DataControllerImpl implements DataController {

    private AppDescriptor appDescriptor;

    public DataControllerImpl(AppDescriptor appDescriptor) {
        this.appDescriptor = appDescriptor;
    }

    @Override
    public void toJson(Object obj, String relativeFilePath) {
        JsonUtils.toJson(obj, Path.of(appDescriptor.getAppProviderDescriptor().getAppDataDir()), relativeFilePath);

    }

    @Override
    public <T> T fromJson(String relativeFilePath, Type type, T defaultObj) {
        return JsonUtils.fromJson(Path.of(appDescriptor.getAppProviderDescriptor().getAppDataDir()), relativeFilePath, type, defaultObj);
    }

}

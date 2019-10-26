package dev.jfxde.data.entity;

import java.util.ArrayList;
import java.util.List;

public class DataRoot {

    private List<Desktop> desktops = new ArrayList<>();
    private List<AppProviderData> appProviders = new ArrayList<>();

    public List<Desktop> getDesktops() {
        return desktops;
    }

    public List<AppProviderData> getAppProviders() {
        return appProviders;
    }
}

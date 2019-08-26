package dev.jfxde.logic.data;

import java.io.FilePermission;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import java.security.Permission;
import java.security.PermissionCollection;
import java.security.Permissions;
import java.security.Policy;
import java.util.HashSet;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.stream.Stream;

import dev.jfxde.api.App;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppScope;
import dev.jfxde.api.PermissionEntry;
import dev.jfxde.api.Resource;
import dev.jfxde.logic.FileManager;
import dev.jfxde.logic.ResourceManager;
import dev.jfxde.logic.Sys;
import dev.jfxde.logic.SystemApp;
import dev.jfxde.logic.security.CustomPolicy;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;

public class AppProviderDescriptor extends DataObj implements Comparable<AppProviderDescriptor> {

    private final AppManifest appManifest;
    private final Provider<App> provider;

    private final CodeSource codeSource;
    private final Path codeSourceParentPath;
    private final BooleanProperty allowed = new SimpleBooleanProperty();
    private final StringProperty name = new SimpleStringProperty();
    private final StringProperty fqn = new SimpleStringProperty();
    private final StringProperty version = new SimpleStringProperty();
    private final StringProperty vendor = new SimpleStringProperty();
    private final StringProperty website = new SimpleStringProperty();
    private final ObjectProperty<Label> iconName = new SimpleObjectProperty<>();

    private ObservableList<AppDescriptor> appDescriptors = FXCollections.observableArrayList();

    private final PermissionCollection permissionCollection = new Permissions();
    private final ObservableList<PermissionDescriptor> permissionDescriptors = FXCollections.observableArrayList();
    private String permissionChecksum = "";
    private boolean system;
    private String appDataDir;
    private ResourceManager resourceManager;
    private Set<String> locales;
    private int match;

    public AppProviderDescriptor(AppManifest appManifest, Provider<App> provider)
            throws Exception {
        this.resourceManager = new ResourceManager(provider.type(), appManifest.altText(), Sys.rm());
        this.appManifest = appManifest;
        this.provider = provider;
        this.name.bind(resourceManager.getTextBinding(appManifest.name()));
        this.fqn.set(appManifest.fqn());
        this.version.set(appManifest.version());
        this.vendor.set(appManifest.vendor());
        this.website.set(appManifest.website());

        this.codeSource = provider.type().getProtectionDomain().getCodeSource();
        this.codeSourceParentPath = Paths.get(codeSource.getLocation().toURI()).getParent();

        this.appDataDir = FileManager.APP_DATA_DIR + "/" + getFqn() + "/" + getVersion();
        Files.createDirectories(Paths.get(appDataDir));

        setSystem(SystemApp.class.isAssignableFrom(provider.type()));
        checkAppPath();
        addPermissions();
    }

    public AppManifest getAppManifest() {
        return appManifest;
    }

    public ObservableList<PermissionDescriptor> getPermissionDescriptors() {
        return permissionDescriptors;
    }

    public ReadOnlyBooleanProperty allowedProperty() {
        return allowed;
    }

    public boolean isAllowed() {
        return allowed.get();
    }

    public void setAllowed(boolean value) {
        allowed.set(value);
    }

    public ReadOnlyStringProperty nameProperty() {
        return name;
    }

    public String getName() {
        return name.get();
    }

    public ReadOnlyStringProperty fqnProperty() {
        return fqn;
    }

    public String getFqn() {
        return fqn.get();
    }

    public CodeSource getCodeSource() {
        return codeSource;
    }

    public ReadOnlyStringProperty versionProperty() {
        return version;
    }

    public String getVersion() {
        return version.get();
    }

    public ReadOnlyStringProperty websiteProperty() {
        return website;
    }

    public ReadOnlyStringProperty vendorProperty() {
        return vendor;
    }

    public PermissionCollection getPermissionCollection() {
        return permissionCollection;
    }

    public String getPermissionChecksum() {
        return permissionChecksum;
    }

    public ResourceManager getResourceManager() {
        return resourceManager;
    }

    public Set<String> getLocales() {

        if (locales == null) {
            locales = new HashSet<>(resourceManager.getLocales());
            locales.add(appManifest.defaultLocale());
        }

        return locales;
    }

    public String getAppDataDir() {
        return appDataDir;
    }

    public ReadOnlyObjectProperty<Label> iconNameProperty() {

        if (iconName.get() == null) {
            Label iconNameLabel = new Label();
            iconNameLabel.textProperty().bind(name);
            iconNameLabel.setGraphic(getSmallIcon());
            iconName.set(iconNameLabel);
        }

        return iconName;
    }

    public boolean isSystem() {
        return system;
    }

    public void setSystem(boolean system) {
        this.system = system;
    }

    public int match(Resource resource) {

        match = 0;

        if (Stream.of(appManifest.uriSchemes()).anyMatch(s -> s.equalsIgnoreCase(resource.getScheme()))) {
            match = 2;
        }

        if (Stream.of(appManifest.fileExtensions()).anyMatch(s -> s.equalsIgnoreCase(resource.getExtension()))) {
            match++;
        }

        return match;
    }

    public int getMatch() {
        return match;
    }

    public void checkAppPath() throws Exception {

        if (!isSystem() && !FileManager.APPS_DIR.equals(codeSourceParentPath)) {
            throw new SecurityException(
                    "App " + getFqn() + " loaded from directory " + codeSourceParentPath);
        }
    }

    public AppDescriptor getAppDescriptor() {
        AppDescriptor appDescriptor = null;

        if (appManifest.scope() == AppScope.PROTOTYPE) {
            appDescriptor = getPrototype();
        } else {
            appDescriptor = getSingleton();
        }

        return appDescriptor;
    }

    public void remove(AppDescriptor appDescriptor) {
        appDescriptors.remove(appDescriptor);
    }

    private AppDescriptor getPrototype() {
        App app = provider.get();
        AppDescriptor appDescriptor = new AppDescriptor(this, app);
        appDescriptors.add(appDescriptor);

        return appDescriptor;
    }

    private AppDescriptor getSingleton() {

        AppDescriptor appDescriptor = null;
        if (appDescriptors.isEmpty()) {
            appDescriptor = getPrototype();
            appDescriptors.add(appDescriptor);
        } else {
            appDescriptor = appDescriptors.get(0);
        }

        return appDescriptor;
    }

    private void addPermissions() {

        if (isSystem()) {
            setAllowed(true);
            return;
        }

        PermissionEntry[] permissionEntries = provider.type().getAnnotationsByType(PermissionEntry.class);

        String checksum = "";

        for (PermissionEntry p : permissionEntries) {
            checksum += p.type().getName() + p.target() + p.actions();
            add(getPermission(p));
        }

        permissionChecksum = checksum.isEmpty() ? "" : Integer.toHexString(checksum.hashCode());
        setAllowed(permissionChecksum.isEmpty());
        add(new FilePermission(Paths.get(appDataDir, "-").toString(), "read,write,delete"));
        putPolicy();
    }

    public void putPolicy() {
        if (isAllowed()) {
            CustomPolicy policy = (CustomPolicy) Policy.getPolicy();
            policy.put(getCodeSource(), getPermissionCollection());
        }
    }

    private void add(Permission permission) {
        PermissionDescriptor descriptor = new PermissionDescriptor(permission);
        permissionDescriptors.add(descriptor);

        permissionCollection.add(permission);
    }

    private Permission getPermission(PermissionEntry entry) {
        Permission permission = null;

        try {
            if (entry.target().isEmpty()) {
                permission = entry.type().getDeclaredConstructor().newInstance();
            } else if (entry.actions().isEmpty()) {
                permission = entry.type().getDeclaredConstructor(String.class).newInstance(entry.target());
            } else {
                permission = entry.type().getDeclaredConstructor(String.class, String.class).newInstance(entry.target(),
                        entry.actions());
            }
        } catch (Exception e) {
            throw new AssertionError(e);
        }
        return permission;
    }

    public Region getMediumIcon() {

        return resourceManager.getMediumIcon("jd-medium-icon");
    }

    public Region getSmallIcon() {

        return resourceManager.getSmallIcon("jd-small-icon");
    }

    @Override
    public int compareTo(AppProviderDescriptor o) {
        return getName().compareTo(o.getName());
    }
}

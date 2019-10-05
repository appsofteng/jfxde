package dev.jfxde.logic;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.ServiceLoader.Provider;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import dev.jfxde.api.App;
import dev.jfxde.api.AppContext;
import dev.jfxde.api.AppManifest;
import dev.jfxde.api.AppRequest;
import dev.jfxde.api.Resource;
import dev.jfxde.data.entity.AppProviderEntity;
import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.logic.context.AppContextImpl;
import dev.jfxde.logic.data.AppDescriptor;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.Window;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener.Change;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;

public final class AppManager extends Manager {

    private final Map<String, AppProviderDescriptor> appProviderDescriptorMap = new HashMap<>();

    private final ObservableList<AppProviderDescriptor> appProviderDescriptors = FXCollections
            .observableArrayList((apd) -> new Observable[] { apd.allowedProperty() });

    private final FilteredList<AppProviderDescriptor> filteredAppProviderDescriptors = new FilteredList<>(
            appProviderDescriptors);
    private final StringProperty appProviderFilter = new SimpleStringProperty();

    private final Map<App, AppDescriptor> appDescriptorMap = new HashMap<>();
    private final ObservableList<AppDescriptor> appDescriptors = FXCollections
            .observableArrayList((ad) -> new Observable[] { ad.displayProperty() });
    private final FilteredList<AppDescriptor> filteredAppDescriptors = new FilteredList<>(
            new SortedList<>(appDescriptors));
    private final StringProperty appFilter = new SimpleStringProperty();
    private ObjectProperty<AppProviderDescriptor> toBeStartedApp = new SimpleObjectProperty<>();
    private Set<String> locales = new TreeSet<>();

    AppManager() {
    }

    @Override
    void init() {
        loadAppProviders();
        setListeners();
    }

    private void loadAppProviders() {

        ServiceLoader<App> loader = ServiceLoader.load(App.class);
        List<Provider<App>> providers = loader.stream().collect(Collectors.toList());

        for (Provider<App> provider : providers) {

            try {
                AppManifest appManifest = provider.type().getAnnotation(AppManifest.class);

                if (appProviderDescriptorMap.containsKey(appManifest.fqn())) {
                    new SecurityException(Sys.rm().getString("duplicateApp") + " " + appManifest.fqn());
                }

                AppProviderDescriptor descriptor = getAppProviderDescriptor(provider);

                if (descriptor != null) {
                    register(descriptor);
                }
            } catch (Throwable throwable) {
                Sys.em().log(throwable);
            }
        }

        FXCollections.sort(appProviderDescriptors);
    }

    private AppProviderDescriptor getAppProviderDescriptor(Provider<App> provider) throws Throwable {
        AppProviderDescriptor descriptor = null;

        AppManifest appManifest = provider.type().getAnnotation(AppManifest.class);

        if (appManifest != null) {
            descriptor = new AppProviderDescriptor(appManifest, provider);
            checkIfAllowed(descriptor);
        }

        return descriptor;
    }

    private void register(AppProviderDescriptor descriptor) {

        locales.addAll(descriptor.getLocales());
        appProviderDescriptorMap.put(descriptor.getFqn(), descriptor);
        appProviderDescriptors.add(descriptor);
    }

    private void checkIfAllowed(AppProviderDescriptor descriptor) {
        List<AppProviderEntity> appProviderEntities = Sys.dm().getAppProviderEntity(descriptor.getFqn());

        if (appProviderEntities.size() == 1) {
            AppProviderEntity appProviderEntity = appProviderEntities.get(0);
            descriptor.setId(appProviderEntity.getId());

            if (appProviderEntity.getPermissionChecksum().equals(descriptor.getPermissionChecksum())) {
                descriptor.setAllowed(appProviderEntity.isAllowed());
                descriptor.putPolicy();
            }
        }

    }

    private void setListeners() {

        appProviderDescriptors.addListener((Change<? extends AppProviderDescriptor> c) -> {

            while (c.next()) {

                if (c.wasUpdated()) {
                    IntStream.range(c.getFrom(), c.getTo()).mapToObj(i -> c.getList().get(i))
                            .forEach(d -> Sys.dm().update(d));
                }
            }
        });

        filteredAppProviderDescriptors.predicateProperty()
                .bind(Bindings.createObjectBinding(
                        () -> d -> (appProviderFilter.isEmpty().get()
                                || d.getName().toLowerCase().contains(appProviderFilter.get().toLowerCase())),
                        appProviderFilter));

        filteredAppDescriptors.predicateProperty()
                .bind(Bindings
                        .createObjectBinding(
                                () -> d -> (appFilter.isEmpty().get()
                                        || d.getDisplay().toLowerCase().contains(appFilter.get().toLowerCase())),
                                appFilter));
    }

    public ReadOnlyObjectProperty<AppProviderDescriptor> toBeStartedApp() {
        return toBeStartedApp;
    }

    public StringProperty appProviderFilterProperty() {
        return appProviderFilter;
    }

    public ObservableList<AppProviderDescriptor> getFilteredAppProviderDescriptors() {
        return filteredAppProviderDescriptors;
    }

    public StringProperty appFilterProperty() {
        return appFilter;
    }

    public ObservableList<AppDescriptor> getFilteredAppDescriptors() {
        return filteredAppDescriptors;
    }

    public ObservableList<AppProviderDescriptor> getAppProviderDescriptors() {
        return appProviderDescriptors;
    }

    public ObservableList<AppDescriptor> getAppDescriptors() {
        return appDescriptors;
    }

    public ObservableList<String> getLocales() {
        ObservableList<String> list = FXCollections.observableArrayList(locales);

        return list;
    }

    public void allowAndStart(AppProviderDescriptor descriptor) {
        allow(descriptor);
        start(descriptor);
    }

    private void allow(AppProviderDescriptor descriptor) {

        descriptor.setAllowed(true);
        descriptor.putPolicy();
    }

    public void start(String uri) {
        Resource resource = new Resource(uri);
        AppProviderDescriptor descriptor = getAppProviderDescriptor(resource);

        if (descriptor != null) {
            AppRequest request = new AppRequest(resource);
            start(null, descriptor, request);
        }
    }

    public void start(String fqn, String uri) {
        AppProviderDescriptor descriptor = getAppProviderDescriptor(fqn);

        if (descriptor != null) {
            Resource resource = new Resource(uri);
            AppRequest request = new AppRequest(resource);
            start(null, descriptor, request);
        }
    }

    public void start(AppDescriptor startingAppDescriptor, String uri) {
        Resource resource = new Resource(uri);
        AppProviderDescriptor descriptor = getAppProviderDescriptor(resource);

        if (descriptor != null) {
            AppRequest request = new AppRequest(resource);
            start(startingAppDescriptor, descriptor, request);
        }
    }

    public void start(AppProviderDescriptor appProviderDescriptor) {
        start(null, appProviderDescriptor, null);
    }

    public void start(AppDescriptor startingAppDescriptor, AppProviderDescriptor appProviderDescriptor, AppRequest request) {

        if (!appProviderDescriptor.isAllowed()) {
            toBeStartedApp.set(null);
            toBeStartedApp.set(appProviderDescriptor);
            return;
        }

        AppDescriptor appDescriptor = appProviderDescriptor.getAppDescriptor();

        if (!appDescriptors.contains(appDescriptor)) {

            AppContext context = new AppContextImpl(appDescriptor, request);
            appDescriptorMap.put(appDescriptor.getApp(), appDescriptor);
            appDescriptors.add(appDescriptor);
            Sys.dm().getActiveDesktop().addWindow(new Window(appDescriptor));

            Task<Void> task = new Task<>() {
                @Override
                protected Void call() throws Exception {
                    appDescriptor.start(context);
                    return null;
                }

                protected void failed() {
                    Sys.em().log(getException());
                };
            };

            Sys.tm().execute(appDescriptor, task);

        } else {
            Sys.dm().getActiveDesktop().addWindow(appDescriptor.getWindow());
        }
    }

    @Override
    void stop() {
        List<AppDescriptor> descripors = new ArrayList<>(appDescriptors);
        descripors.forEach(ad -> stop(ad));
    }

    public void stopAll(List<AppDescriptor> appDescriptors) {

        appDescriptors.forEach(a -> stop(a));
    }

    public void stop(AppDescriptor appDescriptor) {

        Sys.tm().execute(appDescriptor, TaskUtils.createTask(() -> appDescriptor.getApp().stop(), () -> {
            appDescriptor.getWindow().remove();
            appDescriptor.getAppProviderDescriptor().remove(appDescriptor);

            appDescriptorMap.remove(appDescriptor.getApp());
            appDescriptors.remove(appDescriptor);
            Sys.tm().removeTasks(appDescriptor);
        }));
    }

    public void activate(AppDescriptor appDescriptor) {
        if (Sys.dm().setActiveDesktop(appDescriptor.getWindow().getDesktop())) {
            appDescriptor.getWindow().activateUnminimize();
        } else {
            appDescriptor.getWindow().minimizeUnminimize();
        }

    }

    public AppProviderDescriptor getAppProviderDescriptor(String fqn) {
        AppProviderDescriptor descriptor = appProviderDescriptorMap.get(fqn);

        return descriptor;
    }

    // Request, resource, action
    public AppProviderDescriptor getAppProviderDescriptor(Resource resource) {

		List<AppProviderDescriptor> descriptors = appProviderDescriptors.stream()
		        .filter(d -> d.match(resource) > 0)
		        .sorted(Comparator.comparing(AppProviderDescriptor::getMatch).reversed())
		        .collect(Collectors.toList());

		AppProviderDescriptor descriptor = descriptors.isEmpty() ? null : descriptors.get(0);

		return descriptor;
	}

    public void sortApp() {
        FXCollections.sort(appProviderDescriptors);
        FXCollections.sort(appDescriptors);
    }
}

package dev.jfxde.logic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import dev.jfxde.data.dao.AppProviderDao;
import dev.jfxde.data.dao.DesktopDao;
import dev.jfxde.data.dao.ShortcutDao;
import dev.jfxde.data.entity.AppProviderEntity;
import dev.jfxde.data.entity.DesktopEntity;
import dev.jfxde.data.entity.ShortcutEntity;
import dev.jfxde.jfxext.util.TaskUtils;
import dev.jfxde.logic.data.AppProviderDescriptor;
import dev.jfxde.logic.data.DataConvertor;
import dev.jfxde.logic.data.Desktop;
import dev.jfxde.logic.data.Shortcut;
import dev.jfxde.logic.data.Window;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public final class DataManager extends Manager {

	private List<Desktop> desktops = List.of();
	private final ObjectProperty<Desktop> activeDesktop = new SimpleObjectProperty<>();
	private ObjectProperty<Window> activeWindow = new SimpleObjectProperty<>();
	private AppProviderDao appProviderDao;
	private DesktopDao desktopDao;
	private ShortcutDao shortcutDao;
	private EntityManagerFactory emf;
	protected DataConvertor dataConvertor = new DataConvertor();

	DataManager() {
	}

	@Override
	void init() {
		Map<String, Object> configOverrides = new HashMap<>();
		configOverrides.put("javax.persistence.jdbc.url", FileManager.DB_URL);

		emf = Persistence.createEntityManagerFactory("db", configOverrides);

		appProviderDao = new AppProviderDao(emf);
		desktopDao = new DesktopDao(emf);
		shortcutDao = new ShortcutDao(emf);

		initData();
	}

	private void initData() {
		List<DesktopEntity> desktopEntities = desktopDao.getEntities(DesktopEntity.class);

		if (desktopEntities.isEmpty()) {
			desktopEntities = IntStream.range(1, 6).mapToObj(i -> new DesktopEntity(Long.valueOf(i))).collect(Collectors.toList());
			desktopEntities.get(0).setActive(true);
			desktopDao.create(desktopEntities);
			desktopEntities = desktopDao.getEntities(DesktopEntity.class);
		}

		desktops = desktopEntities.stream().map(de -> dataConvertor.convert(de)).collect(Collectors.toList());
		Desktop foundActiveDesktop = desktops.stream().filter(Desktop::isActive).findFirst().get();
		DesktopEntity fetchedDesktopEntity = desktopDao.getDesktop(foundActiveDesktop.getId());
		dataConvertor.convertShortcuts(fetchedDesktopEntity,  foundActiveDesktop);
		foundActiveDesktop.setFetched(true);
		activeDesktop.set(foundActiveDesktop);
		activeWindow.unbind();
		activeWindow.bind( foundActiveDesktop.activeWindowProperty());
		activeDesktop.set(foundActiveDesktop);
	}

	public ObjectProperty<Desktop> activeDesktopProperty() {
		return activeDesktop;
	}

	public Desktop getActiveDesktop() {
		Desktop desktop = activeDesktop.get();

		return desktop;
	}

	public boolean setActiveDesktop(Desktop desktop) {

		if (desktop == getActiveDesktop()) {
			return false;
		}

		if (activeDesktop.get() != null) {
			activeDesktop.get().setActive(false);
			DesktopEntity activeDesktopEntity = dataConvertor.convert(activeDesktop.get());
			Sys.tm().executeSequentially(() -> desktopDao.update(activeDesktopEntity));
		}

		desktop.setActive(true);
		DesktopEntity desktopEntity = dataConvertor.convert(desktop);
		Sys.tm().executeSequentially(() -> desktopDao.update(desktopEntity));

		if (!desktop.isFetched()) {

			Sys.tm().executeSequentially(TaskUtils.createTask(() -> desktopDao.getDesktop(desktop.getId()), (v) -> {
				dataConvertor.convertShortcuts(v, desktop);
				desktop.setFetched(true);
			}));
		}

		activeDesktop.set(desktop);
		activeWindow.unbind();
		activeWindow.bind(desktop.activeWindowProperty());

		return true;
	}

	public ReadOnlyObjectProperty<Window> activeWindowProperty() {
		return activeWindow;
	}

	public Window getActiveWindow() {
		return activeWindow.get();
	}

	public List<Desktop> getDesktops() {

		return desktops;
	}

	public List<AppProviderDescriptor> getAppProviderDescriptors() {

		return List.of();
	}

	public void addShortcut(AppProviderDescriptor descriptor) {
		Shortcut shortcut = new Shortcut(descriptor);

		getActiveDesktop().addShortcut(shortcut);

		ShortcutEntity shortcutEntity = dataConvertor.convert(shortcut);
		Sys.tm().executeSequentially(() -> {
			ShortcutEntity e = shortcutDao.update(shortcutEntity);
			shortcut.setId(e.getId());
		});
	}

	public void removeShortcut(Shortcut shortcut) {

		shortcut.remove();
		Sys.tm().executeSequentially(() -> shortcutDao.delete(ShortcutEntity.class, shortcut.getId()));
	}

	public void updateShortcut(Shortcut shortcut) {
		ShortcutEntity shortcutEntity = dataConvertor.convert(shortcut);
		Sys.tm().executeSequentially(() -> shortcutDao.update(shortcutEntity));
	}

	public List<AppProviderEntity> getAppProviderEntity(String fqn) {

		List<AppProviderEntity> result = appProviderDao.getAppProviderEntity(fqn);

		return result;
	}

	public void update(AppProviderDescriptor descriptor) {

		AppProviderEntity appEntity = dataConvertor.convert(descriptor);
		Sys.tm().executeSequentially(() -> appProviderDao.update(appEntity));
	}

	void stop() {
		emf.close();
	}
}

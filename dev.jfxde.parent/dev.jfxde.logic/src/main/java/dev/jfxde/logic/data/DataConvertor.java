package dev.jfxde.logic.data;

import java.util.stream.Collectors;

import dev.jfxde.data.entity.AppProviderEntity;
import dev.jfxde.data.entity.DesktopEntity;
import dev.jfxde.data.entity.ShortcutEntity;

public class DataConvertor {

	public AppProviderEntity convert(AppProviderDescriptor descriptor) {
		AppProviderEntity appProviderEntity = new AppProviderEntity();
		appProviderEntity.setId(descriptor.getId());
		appProviderEntity.setFqn(descriptor.getFqn());
		appProviderEntity.setAllowed(descriptor.isAllowed());
		appProviderEntity.setPermissionChecksum(descriptor.getPermissionChecksum());

		return appProviderEntity;
	}

	public DesktopEntity convert(Desktop desktop) {
		DesktopEntity desktopEntity = new DesktopEntity();
		desktopEntity.setActive(desktop.isActive());
		desktopEntity.setId(desktop.getId());

		return desktopEntity;
	}

	public Desktop convert(DesktopEntity desktopEntity) {
		Desktop desktop = new Desktop();
		desktop.setActive(desktopEntity.isActive());
		desktop.setId(desktopEntity.getId());

		return desktop;
	}

	public void convertShortcuts(DesktopEntity desktopEntity, Desktop desktop) {
		desktop.getShortcuts().addAll(desktopEntity.getShortcuts().stream().map(se -> convert(se, desktop)).collect(Collectors.toList()));
	}

	public ShortcutEntity convert(Shortcut shortcut) {
		ShortcutEntity shortcutEntity = new ShortcutEntity();

		DesktopEntity desktopEntity = new DesktopEntity();
		desktopEntity.setId(shortcut.getDesktop().getId());
		shortcutEntity.setDesktop(desktopEntity);

		shortcutEntity.setFqn(shortcut.getFqn());
		shortcutEntity.setId(shortcut.getId());
		shortcutEntity.setName(shortcut.getName());
		shortcutEntity.setPosition(shortcut.getPosition());
		shortcutEntity.setUri(shortcut.getUri());

		return shortcutEntity;
	}

	public Shortcut convert(ShortcutEntity shortcutEntity, Desktop desktop) {
		Shortcut shortcut = new Shortcut();

		shortcut.setDesktop(desktop);
		shortcut.setFqn(shortcutEntity.getFqn());
		shortcut.setId(shortcutEntity.getId());
		shortcut.setName(shortcutEntity.getName());
		shortcut.setPosition(shortcutEntity.getPosition());

		return shortcut;
	}
}

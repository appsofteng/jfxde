package dev.jfxde.data.dao;

import javax.persistence.EntityManagerFactory;

import dev.jfxde.data.entity.ShortcutEntity;

public class ShortcutDao extends BaseDao<ShortcutEntity> {
	
	public ShortcutDao(EntityManagerFactory emf) {
		super(emf);
	}
}

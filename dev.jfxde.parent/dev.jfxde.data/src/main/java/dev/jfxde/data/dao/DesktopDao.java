package dev.jfxde.data.dao;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import dev.jfxde.data.entity.DesktopEntity;

public class DesktopDao extends BaseDao<DesktopEntity> {

	public DesktopDao(EntityManagerFactory emf) {
		super(emf);
	}
	
	public DesktopEntity getDesktop(Long id) {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		TypedQuery<DesktopEntity> query = em.createNamedQuery("Desktop.fetch", DesktopEntity.class);

		query.setParameter(1, id);
		DesktopEntity result = query.getSingleResult();

		em.getTransaction().commit();
		em.close();
		
		return result;
	}
}

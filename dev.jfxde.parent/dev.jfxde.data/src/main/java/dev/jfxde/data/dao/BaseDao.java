package dev.jfxde.data.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import dev.jfxde.data.entity.DataEntity;

public class BaseDao<E extends DataEntity> {
	
	protected EntityManagerFactory emf;
	
	public BaseDao(EntityManagerFactory emf) {
		this.emf = emf;
	}
	
	public E create(E entity) {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		em.persist(entity);
		em.getTransaction().commit();
		em.close();

		return entity;
	}
	
	public void create(List<E> entities) {
		
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		
		entities.forEach(e -> em.persist(e));
		
		em.getTransaction().commit();
		em.close();
	}

	public E update(E entity) {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		E merge = em.merge(entity);
		em.getTransaction().commit();
		em.close();
		
		return merge;
	}
	
	public void delete(Class<E> type, Object id) {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();

// Hibernate throws error.
		E toRemove = em.find(type, id);

		if (toRemove != null) {
            em.remove(toRemove);
        }
		
//		em.createQuery("DELETE FROM ShortcutEntity WHERE id = 1").executeUpdate();
		
		em.getTransaction().commit();
		em.close();
	}
	
	public List<E> getEntities(Class<E> entity) {
		EntityManager em = emf.createEntityManager();
		
		em.getTransaction().begin();
		TypedQuery<E> query = em.createQuery(String.format("SELECT e FROM %s e", entity.getSimpleName()), entity);

		List<E> result = query.getResultList();

		em.getTransaction().commit();
		em.close();
		
		return result;
	}
}

package dev.jfxde.data.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;

import dev.jfxde.data.entity.AppProviderEntity;

public class AppProviderDao extends BaseDao<AppProviderEntity> {

    public AppProviderDao(EntityManagerFactory emf) {
        super(emf);
    }

    public List<AppProviderEntity> getAppProviderEntity(String fqn) {

        EntityManager em = emf.createEntityManager();

        em.getTransaction().begin();
        TypedQuery<AppProviderEntity> query = em.createNamedQuery("AppProvider.findByFqn", AppProviderEntity.class);

        query.setParameter(1, fqn);
        List<AppProviderEntity> result = query.getResultList();

        em.getTransaction().commit();
        em.close();

        return result;
    }
}

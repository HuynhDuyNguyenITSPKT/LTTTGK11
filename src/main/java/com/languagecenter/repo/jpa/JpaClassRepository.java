package com.languagecenter.repo.jpa;

import com.languagecenter.model.Class;
import com.languagecenter.repo.ClassRepository;
import jakarta.persistence.EntityManager;
import java.util.List;

public class JpaClassRepository implements ClassRepository {
    @Override
    public List<Class> findAll(EntityManager em) {
        // Sử dụng join fetch để tránh lỗi LazyInitializationException và tối ưu truy vấn
        return em.createQuery(
                "select c from Class c left join fetch c.course left join fetch c.teacher left join fetch c.room order by c.className",
                Class.class
        ).getResultList();
    }

    @Override
    public Class findById(EntityManager em, Long id) {
        return em.find(Class.class, id);
    }

    @Override
    public void create(EntityManager em, Class clazz) {
        em.persist(clazz);
    }

    @Override
    public void update(EntityManager em, Class clazz) {
        em.merge(clazz);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Class c = em.find(Class.class, id);
        if (c != null) em.remove(c);
    }

    @Override
    public List<Class> getByCourse(EntityManager em, Long courseId){

        return em.createQuery("""
            SELECT c
            FROM Class c
            WHERE c.course.id = :courseId
            ORDER BY c.startDate
        """, Class.class)
        .setParameter("courseId", courseId)
        .getResultList();
    }
}
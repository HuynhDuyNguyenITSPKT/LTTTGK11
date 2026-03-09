package com.languagecenter.repo.jpa;

import com.languagecenter.model.Result;
import com.languagecenter.repo.ResultRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaResultRepository implements ResultRepository {

    @Override
    public List<Result> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT r FROM Result r " +
                "LEFT JOIN FETCH r.student " +
                "LEFT JOIN FETCH r.classEntity " +
                "ORDER BY r.classEntity.className, r.student.fullName",
                Result.class
        ).getResultList();
    }

    @Override
    public Result findById(EntityManager em, Long id) {
        return em.find(Result.class, id);
    }

    @Override
    public List<Result> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                "SELECT r FROM Result r " +
                "LEFT JOIN FETCH r.student " +
                "LEFT JOIN FETCH r.classEntity " +
                "WHERE r.student.id = :sid " +
                "ORDER BY r.classEntity.className",
                Result.class
        ).setParameter("sid", studentId).getResultList();
    }

    @Override
    public List<Result> findByClassId(EntityManager em, Long classId) {
        return em.createQuery(
                "SELECT r FROM Result r " +
                "LEFT JOIN FETCH r.student " +
                "LEFT JOIN FETCH r.classEntity " +
                "WHERE r.classEntity.id = :cid " +
                "ORDER BY r.student.fullName",
                Result.class
        ).setParameter("cid", classId).getResultList();
    }

    @Override
    public boolean existsByStudentAndClass(EntityManager em, Long studentId, Long classId) {
        Long count = em.createQuery(
                "SELECT COUNT(r) FROM Result r " +
                "WHERE r.student.id = :sid AND r.classEntity.id = :cid",
                Long.class
        ).setParameter("sid", studentId).setParameter("cid", classId).getSingleResult();
        return count > 0;
    }

    @Override
    public void create(EntityManager em, Result result) {
        em.persist(result);
    }

    @Override
    public void update(EntityManager em, Result result) {
        em.merge(result);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Result r = em.find(Result.class, id);
        if (r != null) em.remove(r);
    }
}

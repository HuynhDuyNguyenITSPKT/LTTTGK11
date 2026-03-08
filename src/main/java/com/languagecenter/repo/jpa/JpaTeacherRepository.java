package com.languagecenter.repo.jpa;

import com.languagecenter.model.Teacher;
import com.languagecenter.repo.TeacherRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaTeacherRepository implements TeacherRepository {

    @Override
    public List<Teacher> findAll(EntityManager em) {
        return em.createQuery(
                "select t from Teacher t order by t.fullName",
                Teacher.class
        ).getResultList();
    }

    @Override
    public Teacher findById(EntityManager em, Long id) {
        return em.find(Teacher.class, id);
    }

    @Override
    public void create(EntityManager em, Teacher teacher) {
        em.persist(teacher);
    }

    @Override
    public void update(EntityManager em, Teacher teacher) {
        em.merge(teacher);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Teacher t = em.find(Teacher.class, id);
        if (t != null) {
            em.remove(t);
        }
    }

    @Override
    public Teacher findByEmail(EntityManager em, String email) {
        List<Teacher> teachers = em.createQuery(
                "select t from Teacher t where t.email = :email",
                Teacher.class
        )
        .setParameter("email", email)
        .getResultList();

        return teachers.isEmpty() ? null : teachers.get(0);
    }
}
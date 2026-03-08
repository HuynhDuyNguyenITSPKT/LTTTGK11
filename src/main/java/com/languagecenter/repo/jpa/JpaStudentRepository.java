package com.languagecenter.repo.jpa;

import com.languagecenter.model.Student;
import com.languagecenter.repo.StudentRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaStudentRepository implements StudentRepository {
    @Override
    public List<Student> findAll(EntityManager em) throws Exception {
        return em.createQuery("select s from Student s order by s.fullName",Student.class)
                .getResultList();
    }

    @Override
    public void create(EntityManager em, Student student) throws Exception {
        em.persist(student);
    }

    @Override
    public Student update(EntityManager em, Student student) throws Exception {
        return em.merge(student);
    }

    @Override
    public void delete(EntityManager em, Long id) throws Exception {
        Student s = em.find(Student.class, id);
        if (s == null) {
            throw new IllegalArgumentException("Student not found: " + id);
        }
        em.remove(s);
    }

    @Override
    public Object findByEmail(EntityManager em, String email) {
        List<Student> students = em.createQuery(
                "select s from Student s where s.email = :email",
                Student.class
        )
        .setParameter("email", email)
        .getResultList();

        return students.isEmpty() ? null : students.get(0);
    }
}

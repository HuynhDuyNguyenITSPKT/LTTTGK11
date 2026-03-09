package com.languagecenter.repo.jpa;

import com.languagecenter.model.Enrollment;
import com.languagecenter.repo.EnrollmentRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaEnrollmentRepository implements EnrollmentRepository {

    @Override
    public List<Enrollment> findAll(EntityManager em) {
        return em.createQuery(
                """
                select e from Enrollment e
                left join fetch e.student
                left join fetch e.classEntity
                order by e.enrollmentDate desc
                """,
                Enrollment.class
        ).getResultList();
    }

    @Override
    public Enrollment findById(EntityManager em, Long id) {
        return em.find(Enrollment.class, id);
    }

    @Override
    public void create(EntityManager em, Enrollment e) {
        em.persist(e);
    }

    @Override
    public void update(EntityManager em, Enrollment e) {
        em.merge(e);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Enrollment e = em.find(Enrollment.class, id);
        if(e != null) em.remove(e);
    }

    @Override
    public boolean existsStudentInClass(EntityManager em, Long studentId, Long classId) {

        Long count = em.createQuery(
                        """
                        select count(e)
                        from Enrollment e
                        where e.student.id = :studentId
                        and e.classEntity.id = :classId
                        """,
                        Long.class
                )
                .setParameter("studentId", studentId)
                .setParameter("classId", classId)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public Long countStudentsByClass(EntityManager em, Long classId){

        return em.createQuery("""
        SELECT COUNT(e)
        FROM Enrollment e
        WHERE e.classEntity.id = :classId
    """, Long.class)
                .setParameter("classId", classId)
                .getSingleResult();
    }


    @Override
    public boolean existsStudentInCourse(EntityManager em, Long studentId, Long courseId){

        Long count = em.createQuery("""
            SELECT COUNT(e)
            FROM Enrollment e
            WHERE e.student.id = :studentId
            AND e.classEntity.course.id = :courseId
        """, Long.class)
        .setParameter("studentId", studentId)
        .setParameter("courseId", courseId)
        .getSingleResult();

        return count > 0;
    }
}
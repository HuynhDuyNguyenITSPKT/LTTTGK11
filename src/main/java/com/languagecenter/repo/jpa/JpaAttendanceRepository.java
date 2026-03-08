package com.languagecenter.repo.jpa;

import com.languagecenter.model.Attendance;
import com.languagecenter.repo.AttendanceRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public class JpaAttendanceRepository implements AttendanceRepository {

    @Override
    public List<Attendance> findAll(EntityManager em) {
        return em.createQuery(
                """
                select a from Attendance a
                left join fetch a.student
                left join fetch a.classEntity
                order by a.attendDate desc, a.student.fullName
                """,
                Attendance.class
        ).getResultList();
    }

    @Override
    public Attendance findById(EntityManager em, Long id) {
        return em.find(Attendance.class, id);
    }

    @Override
    public void create(EntityManager em, Attendance attendance) {
        em.persist(attendance);
    }

    @Override
    public void update(EntityManager em, Attendance attendance) {
        em.merge(attendance);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Attendance attendance = em.find(Attendance.class, id);
        if (attendance != null) {
            em.remove(attendance);
        }
    }

    @Override
    public List<Attendance> findByClassId(EntityManager em, Long classId) {
        return em.createQuery(
                """
                select a from Attendance a
                left join fetch a.student
                left join fetch a.classEntity
                where a.classEntity.id = :classId
                order by a.attendDate desc, a.student.fullName
                """,
                Attendance.class
        ).setParameter("classId", classId).getResultList();
    }

    @Override
    public List<Attendance> findByStudentId(EntityManager em, Long studentId) {
        return em.createQuery(
                """
                select a from Attendance a
                left join fetch a.student
                left join fetch a.classEntity
                where a.student.id = :studentId
                order by a.attendDate desc
                """,
                Attendance.class
        ).setParameter("studentId", studentId).getResultList();
    }

    @Override
    public List<Attendance> findByClassAndDate(EntityManager em, Long classId, LocalDate date) {
        return em.createQuery(
                """
                select a from Attendance a
                left join fetch a.student
                left join fetch a.classEntity
                where a.classEntity.id = :classId
                and a.attendDate = :date
                order by a.student.fullName
                """,
                Attendance.class
        )
                .setParameter("classId", classId)
                .setParameter("date", date)
                .getResultList();
    }

    @Override
    public boolean existsByStudentClassAndDate(EntityManager em, Long studentId, Long classId, LocalDate date) {
        Long count = em.createQuery(
                        """
                        select count(a)
                        from Attendance a
                        where a.student.id = :studentId
                        and a.classEntity.id = :classId
                        and a.attendDate = :date
                        """,
                        Long.class
                )
                .setParameter("studentId", studentId)
                .setParameter("classId", classId)
                .setParameter("date", date)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public long countByClassAndDate(EntityManager em, Long classId, LocalDate date) {
        return em.createQuery(
                        """
                        select count(a)
                        from Attendance a
                        where a.classEntity.id = :classId
                        and a.attendDate = :date
                        """,
                        Long.class
                )
                .setParameter("classId", classId)
                .setParameter("date", date)
                .getSingleResult();
    }
}

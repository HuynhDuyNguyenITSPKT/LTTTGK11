package com.languagecenter.repo.jpa;

import com.languagecenter.model.Schedule;
import com.languagecenter.repo.ScheduleRepository;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class JpaScheduleRepository implements ScheduleRepository {

    @Override
    public List<Schedule> findAll(EntityManager em) {
        return em.createQuery(
                "select s from Schedule s " +
                        "left join fetch s.classEntity " +
                        "left join fetch s.room " +
                        "order by s.studyDate",
                Schedule.class
        ).getResultList();
    }

    @Override
    public Schedule findById(EntityManager em, Long id) {
        return em.find(Schedule.class, id);
    }

    @Override
    public void create(EntityManager em, Schedule schedule) {
        em.persist(schedule);
    }

    @Override
    public void update(EntityManager em, Schedule schedule) {
        em.merge(schedule);
    }

    @Override
    public void delete(EntityManager em, Long id) {
        Schedule s = em.find(Schedule.class, id);
        if (s != null) em.remove(s);
    }

    @Override
    public boolean existsRoomConflict(EntityManager em,
                                      Long roomId,
                                      LocalDate date,
                                      LocalTime start,
                                      LocalTime end) {

        Long count = em.createQuery(
                        """
                        select count(s)
                        from Schedule s
                        where s.room.id = :roomId
                        and s.studyDate = :date
                        and (s.startTime < :end and s.endTime > :start)
                        """,
                        Long.class
                )
                .setParameter("roomId", roomId)
                .setParameter("date", date)
                .setParameter("start", start)
                .setParameter("end", end)
                .getSingleResult();

        return count > 0;
    }

    @Override
    public List<Schedule> getScheduleByStudent(EntityManager em, Long studentId) {

        return em.createQuery(
                        """
                        select s
                        from Schedule s
                        join fetch s.classEntity c
                        join fetch c.teacher t
                        join fetch s.room
                        join Enrollment e on e.classEntity.id = c.id
                        where e.student.id = :studentId
                        order by s.studyDate, s.startTime
                        """,
                        Schedule.class
                )
                .setParameter("studentId", studentId)
                .getResultList();
    }
}
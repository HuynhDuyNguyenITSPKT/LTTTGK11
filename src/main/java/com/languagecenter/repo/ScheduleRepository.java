package com.languagecenter.repo;

import com.languagecenter.model.Schedule;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ScheduleRepository {

    List<Schedule> findAll(EntityManager em) throws Exception;

    Schedule findById(EntityManager em, Long id) throws Exception;

    void create(EntityManager em, Schedule schedule) throws Exception;

    void update(EntityManager em, Schedule schedule) throws Exception;

    void delete(EntityManager em, Long id) throws Exception;

    boolean existsRoomConflict(EntityManager em,
                               Long roomId,
                               LocalDate date,
                               LocalTime start,
                               LocalTime end);
    List<Schedule> getScheduleByStudent(EntityManager em, Long studentId);

    List<Schedule> getScheduleByTeacher(EntityManager em, Long teacherId);

    public boolean checkTeacherScheduleConflict(EntityManager em);
}
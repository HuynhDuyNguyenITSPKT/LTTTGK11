package com.languagecenter.repo;

import com.languagecenter.model.Attendance;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceRepository {

    List<Attendance> findAll(EntityManager em);

    Attendance findById(EntityManager em, Long id);

    void create(EntityManager em, Attendance attendance);

    void update(EntityManager em, Attendance attendance);

    void delete(EntityManager em, Long id);

    List<Attendance> findByClassId(EntityManager em, Long classId);

    List<Attendance> findByStudentId(EntityManager em, Long studentId);

    List<Attendance> findByClassAndDate(EntityManager em, Long classId, LocalDate date);

    boolean existsByStudentClassAndDate(EntityManager em, Long studentId, Long classId, LocalDate date);

    long countByClassAndDate(EntityManager em, Long classId, LocalDate date);
}

package com.languagecenter.repo;

import com.languagecenter.model.Enrollment;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface EnrollmentRepository {

    List<Enrollment> findAll(EntityManager em);

    Enrollment findById(EntityManager em, Long id);

    void create(EntityManager em, Enrollment e);

    void update(EntityManager em, Enrollment e);

    void delete(EntityManager em, Long id);

    boolean existsStudentInClass(EntityManager em, Long studentId, Long classId);

    public Long countStudentsByClass(EntityManager em, Long classId);
}
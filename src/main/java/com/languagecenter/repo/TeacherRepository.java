package com.languagecenter.repo;

import com.languagecenter.model.Teacher;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface TeacherRepository {

    List<Teacher> findAll(EntityManager em) throws Exception;

    void create(EntityManager em, Teacher teacher) throws Exception;

    void update(EntityManager em, Teacher teacher) throws Exception;

    void delete(EntityManager em, Long id) throws Exception;

    Teacher findById(EntityManager em, Long id) throws Exception;

    Teacher findByEmail(EntityManager em, String email);

}
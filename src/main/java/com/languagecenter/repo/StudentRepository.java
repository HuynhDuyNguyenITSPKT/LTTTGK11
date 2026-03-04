package com.languagecenter.repo;

import com.languagecenter.model.Student;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface StudentRepository {
    List<Student> findAll(EntityManager em) throws Exception;
    void create(EntityManager em, Student student) throws Exception;
    Student update(EntityManager em, Student student) throws Exception;
    void delete(EntityManager em, Long id) throws Exception;
}

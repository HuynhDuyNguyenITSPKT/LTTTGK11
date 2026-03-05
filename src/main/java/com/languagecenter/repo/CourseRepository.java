package com.languagecenter.repo;

import com.languagecenter.model.Course;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface CourseRepository {

    List<Course> findAll(EntityManager em) throws Exception;

    Course findById(EntityManager em, Long id) throws Exception;

    void create(EntityManager em, Course course) throws Exception;

    void update(EntityManager em, Course course) throws Exception;

    void delete(EntityManager em, Long id) throws Exception;
}
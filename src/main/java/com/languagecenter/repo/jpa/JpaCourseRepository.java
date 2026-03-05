package com.languagecenter.repo.jpa;

import com.languagecenter.model.Course;
import com.languagecenter.repo.CourseRepository;
import jakarta.persistence.EntityManager;

import java.util.List;

public class JpaCourseRepository implements CourseRepository {

    @Override
    public List<Course> findAll(EntityManager em) {

        return em.createQuery(
                "select c from Course c order by c.courseName",
                Course.class
        ).getResultList();
    }

    @Override
    public Course findById(EntityManager em, Long id) {

        return em.find(Course.class, id);
    }

    @Override
    public void create(EntityManager em, Course course) {

        em.persist(course);
    }

    @Override
    public void update(EntityManager em, Course course) {

        em.merge(course);
    }

    @Override
    public void delete(EntityManager em, Long id) {

        Course c = em.find(Course.class, id);

        if (c != null) {
            em.remove(c);
        }
    }
}
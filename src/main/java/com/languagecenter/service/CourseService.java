package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Course;
import com.languagecenter.repo.CourseRepository;

import java.util.List;

public class CourseService {

    private final CourseRepository repo;
    private final TransactionManager tx;

    public CourseService(CourseRepository repo,
                         TransactionManager tx){

        this.repo = repo;
        this.tx = tx;
    }

    public List<Course> getAll() throws Exception {

        return tx.runInTransaction(
                em -> repo.findAll(em)
        );
    }

    public void create(Course course) throws Exception {

        tx.runInTransaction(em -> {

            repo.create(em,course);

            return null;
        });
    }

    public void update(Course course) throws Exception {

        tx.runInTransaction(em -> {

            repo.update(em,course);

            return null;
        });
    }

    public void delete(Long id) throws Exception {

        tx.runInTransaction(em -> {

            repo.delete(em,id);

            return null;
        });
    }
}
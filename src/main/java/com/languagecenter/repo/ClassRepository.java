package com.languagecenter.repo;

import com.languagecenter.model.Class;
import jakarta.persistence.EntityManager;
import java.util.List;

public interface ClassRepository {
    List<Class> findAll(EntityManager em) throws Exception;
    Class findById(EntityManager em, Long id) throws Exception;
    void create(EntityManager em, Class clazz) throws Exception;
    void update(EntityManager em, Class clazz) throws Exception;
    void delete(EntityManager em, Long id) throws Exception;
    List<Class> getByCourse(EntityManager em, Long courseId);
}
package com.languagecenter.repo;

import com.languagecenter.model.Result;
import jakarta.persistence.EntityManager;

import java.util.List;

public interface ResultRepository {

    List<Result> findAll(EntityManager em) throws Exception;

    Result findById(EntityManager em, Long id) throws Exception;

    List<Result> findByStudentId(EntityManager em, Long studentId) throws Exception;

    List<Result> findByClassId(EntityManager em, Long classId) throws Exception;

    boolean existsByStudentAndClass(EntityManager em, Long studentId, Long classId) throws Exception;

    void create(EntityManager em, Result result) throws Exception;

    void update(EntityManager em, Result result) throws Exception;

    void delete(EntityManager em, Long id) throws Exception;
}

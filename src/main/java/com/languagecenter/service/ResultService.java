package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Result;
import com.languagecenter.repo.ResultRepository;

import java.util.List;

public class ResultService {

    private final ResultRepository repo;
    private final TransactionManager tx;

    public ResultService(ResultRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx   = tx;
    }

    /** All results — admin/teacher use-case */
    public List<Result> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /** Results by student — student portal / admin detail */
    public List<Result> getByStudent(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    /** Results by class — teacher entry panel */
    public List<Result> getByClass(Long classId) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassId(em, classId));
    }

    /** Create — admin or teacher.
     *  Enforces unique (student, class) constraint before persisting. */
    public void create(Result result) throws Exception {
        tx.runInTransaction(em -> {
            Long sid = result.getStudent().getId();
            Long cid = result.getClassEntity().getId();
            if (repo.existsByStudentAndClass(em, sid, cid)) {
                throw new IllegalStateException(
                        "A result for this student in this class already exists.");
            }
            // Attach managed references before persisting
            result.setStudent(em.find(com.languagecenter.model.Student.class, sid));
            result.setClassEntity(em.find(com.languagecenter.model.Class.class, cid));
            repo.create(em, result);
            return null;
        });
    }

    /** Update — admin or teacher */
    public void update(Result result) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, result);
            return null;
        });
    }

    /** Delete — admin only */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }
}

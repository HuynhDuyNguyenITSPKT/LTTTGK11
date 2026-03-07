package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.enums.EnrollmentStatus;
import com.languagecenter.model.enums.ResultStatus;
import com.languagecenter.repo.EnrollmentRepository;

import java.util.List;

public class EnrollmentService {

    private final EnrollmentRepository repo;
    private final TransactionManager tx;

    public EnrollmentService(EnrollmentRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    public List<Enrollment> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    public void create(Enrollment e) throws Exception {

        tx.runInTransaction(em -> {

            boolean exists = repo.existsStudentInClass(
                    em,
                    e.getStudent().getId(),
                    e.getClassEntity().getId()
            );

            if(exists)
                throw new Exception("Student đã đăng ký lớp này!");

            if(e.getStatus() == EnrollmentStatus.Dropped)
                e.setResult(ResultStatus.NA);

            repo.create(em,e);

            return null;
        });
    }

    public void update(Enrollment e) throws Exception {

        tx.runInTransaction(em -> {

            if(e.getStatus() == EnrollmentStatus.Dropped)
                e.setResult(ResultStatus.NA);

            repo.update(em,e);

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
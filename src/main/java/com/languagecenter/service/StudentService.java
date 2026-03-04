package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Student;
import com.languagecenter.repo.StudentRepository;

import java.util.List;

public class StudentService {
    private final StudentRepository studentRepo;
    private final TransactionManager tx;

    public StudentService(StudentRepository studentRepo, TransactionManager tx) {
        this.studentRepo = studentRepo;
        this.tx = tx;
    }

    public List<Student> getAll() throws Exception{
        return tx.runInTransaction(
                em -> studentRepo.findAll(em)
        );
    }

    public void create(Student student) throws Exception {
        tx.runInTransaction(em -> {
            studentRepo.create(em, student);
            return null;
        });
    }

    public void update(Student student) throws Exception {
        tx.runInTransaction(em -> {
            studentRepo.update(em, student);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            studentRepo.delete(em, id);
            return null;
        });
    }
}

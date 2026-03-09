package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Class;
import com.languagecenter.repo.ClassRepository;
import java.util.List;

public class ClassService {
    private final ClassRepository classRepo;
    private final TransactionManager tx;

    public ClassService(ClassRepository classRepo, TransactionManager tx) {
        this.classRepo = classRepo;
        this.tx = tx;
    }

    public List<Class> getAll() throws Exception {
        return tx.runInTransaction(classRepo::findAll);
    }

    public void create(Class clazz) throws Exception {
        tx.runInTransaction(em -> {
            if (clazz.getMaxStudent() > clazz.getRoom().getCapacity()){
                throw new Exception("Số lượng học viên tối đa không được vượt quá sức chứa của phòng học"+
                        " (Max Student: "+clazz.getMaxStudent()+", Room Capacity: "+clazz.getRoom().getCapacity()+")");
            }
            classRepo.create(em, clazz);
            return null;
        });
    }

    public void update(Class clazz) throws Exception {
        tx.runInTransaction(em -> {
            classRepo.update(em, clazz);
            return null;
        });
    }

    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            classRepo.delete(em, id);
            return null;
        });
    }

    public List<Class> getByCourse(Long courseId) throws Exception {
    return tx.runInTransaction(em ->
        classRepo.getByCourse(em, courseId)
    );
}
}
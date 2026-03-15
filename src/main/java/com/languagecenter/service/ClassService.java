package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Class;
import com.languagecenter.repo.ClassRepository;
import java.util.List;

/**
 * Xử lý các thao tác truy xuất và cập nhật dữ liệu của Lớp Học (Class).
 * <p>
 * Hệ thống tuân theo chuẩn SOLID, đảm bảo class này chỉ tập trung vào nghiệp vụ quản lý lớp học.
 * </p>
 */
public class ClassService {
    private final ClassRepository classRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service bằng Dependency Injection cho repo và transaction manager.
     *
     */
    public ClassService(ClassRepository classRepo, TransactionManager tx) {
        this.classRepo = classRepo;
        this.tx = tx;
    }

    /**
     * Lấy danh sách toàn bộ các lớp học có trong hệ thống.
     *
     */
    public List<Class> getAll() throws Exception {
        return tx.runInTransaction(classRepo::findAll);
    }

    /**
     * Tạo mới một lớp học, kèm theo nghiệp vụ kiểm tra tính hợp lệ cơ bản.
     *
     */
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

    /**
     * Cập nhật thông tin lớp học vào DB.
     *
     */
    public void update(Class clazz) throws Exception {
        tx.runInTransaction(em -> {
            classRepo.update(em, clazz);
            return null;
        });
    }

    /**
     * Xóa một lớp học dựa theo ID.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            classRepo.delete(em, id);
            return null;
        });
    }

    /**
     * Lấy các lớp học liên quan đến một khóa học chỉ định.
     *
     */
    public List<Class> getByCourse(Long courseId) throws Exception {
        return tx.runInTransaction(em -> classRepo.getByCourse(em, courseId));
    }
}
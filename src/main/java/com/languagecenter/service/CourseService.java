package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Course;
import com.languagecenter.repo.CourseRepository;

import java.util.List;

/**
 * Xử lý nghiệp vụ liên quan đến quản lý Khóa Học (Course).
 * <p>
 * Class này giữ nhiệm vụ duy nhất là giao tiếp xử lý các thao tác khóa học
 * qua Repository, tuân thủ nguyên tắc SRP trong thiết kế SOLID.
 * </p>
 */
public class CourseService {

    private final CourseRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service cho khóa học.
     *
     */
    public CourseService(CourseRepository repo, TransactionManager tx){
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Trích xuất toàn bộ dữ liệu khóa học có trong hệ thống.
     *
     */
    public List<Course> getAll() throws Exception {
        return tx.runInTransaction(em -> repo.findAll(em));
    }

    /**
     * Bắt đầu một giao dịch để thêm khóa học mới vào cơ sở dữ liệu.
     *
     */
    public void create(Course course) throws Exception {
        tx.runInTransaction(em -> {
            repo.create(em, course);
            return null;
        });
    }

    /**
     * Cập nhật thông tin của khóa học, cần có ID để xác định bản ghi hiện tại.
     *
     */
    public void update(Course course) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, course);
            return null;
        });
    }

    /**
     * Xóa khóa học bằng cách sử dụng cấu trúc ID.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }
}
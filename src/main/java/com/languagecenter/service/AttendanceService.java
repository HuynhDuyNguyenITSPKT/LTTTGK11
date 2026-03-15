package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Attendance;
import com.languagecenter.repo.AttendanceRepository;

import java.time.LocalDate;
import java.util.List;

/**
 * Cung cấp các thao tác nghiệp vụ cho đối tượng điểm danh (Attendance).
 * <p>
 * Class này tuân thủ Single Responsibility Principle (SRP) bằng cách chỉ xử lý
 * nghiệp vụ điểm danh và giao tiếp với tầng repository thông qua TransactionManager.
 * </p>
 */
public class AttendanceService {

    private final AttendanceRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service với repository và transaction manager sử dụng Dependency Injection.
     *
     */
    public AttendanceService(AttendanceRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Lấy danh sách toàn bộ điểm danh.
     *
     */
    public List<Attendance> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Lấy thông tin điểm danh theo ID.
     *
     */
    public Attendance getById(Long id) throws Exception {
        return tx.runInTransaction(em -> repo.findById(em, id));
    }

    /**
     * Tạo mới một đối tượng điểm danh.
     *
     */
    public void create(Attendance attendance) throws Exception {
        tx.runInTransaction(em -> {
            boolean exists = repo.existsByStudentClassAndDate(
                    em,
                    attendance.getStudent().getId(),
                    attendance.getClassEntity().getId(),
                    attendance.getAttendDate()
            );

            if (exists) {
                throw new Exception("Attendance already exists for this student on this date!");
            }

            repo.create(em, attendance);
            return null;
        });
    }

    /**
     * Cập nhật thông tin điểm danh.
     *
     */
    public void update(Attendance attendance) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, attendance);
            return null;
        });
    }

    /**
     * Xóa thông tin điểm danh.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }

    /**
     * Lấy danh sách điểm danh theo mã lớp.
     *
     */
    public List<Attendance> getByClassId(Long classId) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassId(em, classId));
    }

    /**
     * Lấy danh sách điểm danh của học sinh.
     *
     */
    public List<Attendance> getByStudentId(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    /**
     * Lấy thông tin điểm danh của một lớp nào đó trong một ngày cụ thể.
     *
     */
    public List<Attendance> getByClassAndDate(Long classId, LocalDate date) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassAndDate(em, classId, date));
    }

    /**
     * Đếm tổng số điểm danh của lớp trong một ngày.
     *
     */
    public long countByClassAndDate(Long classId, LocalDate date) throws Exception {
        return tx.runInTransaction(em -> repo.countByClassAndDate(em, classId, date));
    }
}

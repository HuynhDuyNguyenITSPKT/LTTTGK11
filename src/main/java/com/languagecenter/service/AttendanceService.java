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
     * @param repo Repository truy xuất dữ liệu trạng thái điểm danh
     * @param tx   Công cụ quản lý giao dịch
     */
    public AttendanceService(AttendanceRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Lấy danh sách toàn bộ điểm danh.
     *
     * @return Danh sách các đối tượng Attendance
     * @throws Exception Số lỗi gặp phải khi lấy dữ liệu
     */
    public List<Attendance> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Lấy thông tin điểm danh theo ID.
     *
     * @param id ID của điểm danh cần tìm
     * @return Đối tượng Attendance nếu tồn tại, ngược lại trả về null
     * @throws Exception Lỗi xảy ra trong quá trình truy vấn
     */
    public Attendance getById(Long id) throws Exception {
        return tx.runInTransaction(em -> repo.findById(em, id));
    }

    /**
     * Tạo mới một đối tượng điểm danh.
     *
     * @param attendance Đối tượng Attendance cần tạo
     * @throws Exception Nếu học sinh đã được điểm danh trong ngày này và lớp này, hoặc lỗi hệ thống
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
     * @param attendance Đối tượng Attendance chứa thông tin cập nhật
     * @throws Exception Nếu lỗi cập nhật dữ liệu vào DB
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
     * @param id ID của điểm danh cần xóa
     * @throws Exception Lỗi trong quá trình xóa dữ liệu
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
     * @param classId ID lớp học
     * @return Danh sách điểm danh thuộc lớp được chỉ định
     * @throws Exception Lỗi khi truy vấn
     */
    public List<Attendance> getByClassId(Long classId) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassId(em, classId));
    }

    /**
     * Lấy danh sách điểm danh của học sinh.
     *
     * @param studentId ID học sinh
     * @return Danh sách điểm danh của học sinh đó
     * @throws Exception Lỗi trong quá trình thực thi khối truy vấn
     */
    public List<Attendance> getByStudentId(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    /**
     * Lấy thông tin điểm danh của một lớp nào đó trong một ngày cụ thể.
     *
     * @param classId Mã lớp học
     * @param date    Ngày được điểm danh
     * @return Danh sách các điểm danh khớp điều kiện
     * @throws Exception Lỗi kết nối
     */
    public List<Attendance> getByClassAndDate(Long classId, LocalDate date) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassAndDate(em, classId, date));
    }

    /**
     * Đếm tổng số điểm danh của lớp trong một ngày.
     *
     * @param classId Mã lớp
     * @param date    Ngày kiểm tra điểm danh
     * @return Tổng số lượng điểm danh
     * @throws Exception Lỗi khi truy vấn số lượng
     */
    public long countByClassAndDate(Long classId, LocalDate date) throws Exception {
        return tx.runInTransaction(em -> repo.countByClassAndDate(em, classId, date));
    }
}

package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Result;
import com.languagecenter.repo.ResultRepository;

import java.util.List;

/**
 * Xử lý các thao tác về kết quả học tập của học viên.
 * <p>
 * Hệ thống sử dụng class này để đảm bảo rằng kết quả học tập
 * được lưu lại, cập nhật hoặc xóa đi một cách an toàn nhất theo 
 * quy tắc tập trung Single Responsibility Principle.
 * </p>
 */
public class ResultService {

    private final ResultRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo ResultService bằng việc inject các dependencies.
     *
     * @param repo Repository tương tác với bảng Result
     * @param tx   Công cụ quản lý giao dịch
     */
    public ResultService(ResultRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx   = tx;
    }

    /**
     * Lấy toàn bộ danh sách kết quả học tập từ database.
     *
     * @return Danh sách các kết quả
     * @throws Exception Lỗi mạng
     */
    public List<Result> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Lấy danh sách kết quả học tập của một học viên cụ thể.
     *
     * @param studentId ID học viên 
     * @return Danh sách các kết quả thuộc về học sinh
     * @throws Exception Trả về lỗi khi quá trình truy vấn thất bại
     */
    public List<Result> getByStudent(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    /**
     * Lấy danh sách bảng điểm của một lớp học.
     *
     * @param classId ID lớp học
     * @return Danh sách điểm của nhóm học sinh trong lớp
     * @throws Exception Các lỗi đọc dữ liệu từ repo
     */
    public List<Result> getByClass(Long classId) throws Exception {
        return tx.runInTransaction(em -> repo.findByClassId(em, classId));
    }

    /**
     * Khởi tạo bản ghi điểm học tập mới.
     * Hệ thống sẽ tự động bắt ràng buộc về tính duy nhất của một tài khoản trên cùng lớp học.
     *
     * @param result Bản ghi điểm của sinh viên đó
     * @throws Exception Lỗi về việc bảng điểm đã tồn tại hoặc lỗi thao tác DB.
     */
    public void create(Result result) throws Exception {
        tx.runInTransaction(em -> {
            Long sid = result.getStudent().getId();
            Long cid = result.getClassEntity().getId();
            if (repo.existsByStudentAndClass(em, sid, cid)) {
                throw new IllegalStateException(
                        "Kết quả của học viên này trong lớp học đã tồn tại.");
            }
            result.setStudent(em.find(com.languagecenter.model.Student.class, sid));
            result.setClassEntity(em.find(com.languagecenter.model.Class.class, cid));
            repo.create(em, result);
            return null;
        });
    }

    /**
     * Cập nhật điểm cho sinh viên vào kho dữ liệu.
     *
     * @param result Bản điểm bị thay đổi của đối tượng học sinh
     * @throws Exception Trả ra khi không tìm thấy record
     */
    public void update(Result result) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, result);
            return null;
        });
    }

    /**
     * Xóa danh mục kết quả của sinh viên trong một khoá học.
     *
     * @param id Khóa chính của bảng
     * @throws Exception Các lỗi phụ thuộc dữ liệu khoá
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }
}

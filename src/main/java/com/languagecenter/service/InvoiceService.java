package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Invoice;
import com.languagecenter.repo.InvoiceRepository;

import java.util.List;

/**
 * Xử lý nghiệp vụ hoá đơn, bao gồm lưu trữ, cập nhật, lấy thông tin.
 * <p>
 * Class này tuân theo Single Responsibility Principle (SRP) bằng cách
 * chuyên biệt hoá quá trình giao tiếp dữ liệu Invoice xuống database.
 * </p>
 */
public class InvoiceService {

    private final InvoiceRepository repo;
    private final TransactionManager tx;

    /**
     * Khởi tạo InvoiceService.
     *
     * @param repo Repository hoá đơn
     * @param tx   Công cụ quản lý giao dịch
     */
    public InvoiceService(InvoiceRepository repo, TransactionManager tx) {
        this.repo = repo;
        this.tx = tx;
    }

    /**
     * Lấy toàn bộ danh sách hóa đơn hiện có.
     *
     * @return Danh sách các Invoice
     * @throws Exception Lỗi kết nối
     */
    public List<Invoice> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Lấy hóa đơn theo ID.
     *
     * @param id Khóa chính của bảng hoá đơn
     * @return Hoá đơn tìm được
     * @throws Exception Các lỗi thao tác dữ liệu
     */
    public Invoice getById(Long id) throws Exception {
        return tx.runInTransaction(em -> repo.findById(em, id));
    }

    /**
     * Lấy hóa đơn dựa theo mã Đăng ký (Enrollment).
     *
     * @param enrollmentId ID đăng ký khoá học
     * @return Hoá đơn thuộc Enrollment này
     * @throws Exception Lỗi thao tác
     */
    public Invoice getByEnrollmentId(Long enrollmentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByEnrollmentId(em, enrollmentId));
    }

    /**
     * Lấy danh sách hóa đơn dựa theo học sinh.
     *
     * @param studentId ID học sinh
     * @return Danh sách hoá đơn tương ứng
     * @throws Exception Lỗi thao tác
     */
    public List<Invoice> getByStudentId(Long studentId) throws Exception {
        return tx.runInTransaction(em -> repo.findByStudentId(em, studentId));
    }

    /**
     * Thêm mới một hóa đơn vào thư viện CSDL.
     *
     * @param invoice Thông tin hoá đơn
     * @throws Exception Lỗi khởi tạo
     */
    public void create(Invoice invoice) throws Exception {
        tx.runInTransaction(em -> {
            repo.create(em, invoice);
            return null;
        });
    }

    /**
     * Cập nhật thông tin trên một hoá đơn đã có.
     *
     * @param invoice Thông tin hoá đơn với dữ liệu mới
     * @throws Exception Lỗi thao tác update
     */
    public void update(Invoice invoice) throws Exception {
        tx.runInTransaction(em -> {
            repo.update(em, invoice);
            return null;
        });
    }

    /**
     * Xóa hoá đơn dựa vào ID.
     *
     * @param id Định danh hóa đơn
     * @throws Exception Lỗi ràng buộc hệ thống
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em, id);
            return null;
        });
    }
}

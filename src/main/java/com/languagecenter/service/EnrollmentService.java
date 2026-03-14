package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Enrollment;
import com.languagecenter.model.Invoice;
import com.languagecenter.model.Student;
import com.languagecenter.model.Class;
import com.languagecenter.model.enums.ClassStatus;
import com.languagecenter.model.enums.EnrollmentStatus;
import com.languagecenter.model.enums.InvoiceStatus;
import com.languagecenter.model.enums.ResultStatus;
import com.languagecenter.repo.EnrollmentRepository;
import com.languagecenter.repo.InvoiceRepository;

import jakarta.transaction.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Xử lý nghiệp vụ Ghi Danh (Enrollment), đi kèm với việc cấp chi phí (Invoice) ngay khi ghi danh.
 * <p>
 * Class này phối hợp hai thao tác độc lập nhưng liên đới mật thiết với nhau
 * trong cùng một transaction theo chuẩn thiết kế.
 * </p>
 */
public class EnrollmentService {

    private final EnrollmentRepository repo;
    private final InvoiceRepository invoiceRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo service cung cấp cho việc giao tiếp và cấu hình CSDL ở bảng Enrollment, Invoice.
     *
     * @param repo        Repository đăng ký học
     * @param invoiceRepo Repository phục vụ sinh hóa đơn học phí
     * @param tx          Máy cung cấp Session
     */
    public EnrollmentService(EnrollmentRepository repo,
                            InvoiceRepository invoiceRepo,
                            TransactionManager tx) {
        this.repo = repo;
        this.invoiceRepo = invoiceRepo;
        this.tx = tx;
    }

    /**
     * Lấy các đăng ký có trong hệ thống hiện tại.
     *
     * @return Danh sách các bản ghi danh
     * @throws Exception Lỗi mạng
     */
    public List<Enrollment> getAll() throws Exception {
        return tx.runInTransaction(repo::findAll);
    }

    /**
     * Khởi tạo bản ghi đánh dấu đăng ký vào lớp học. Sẽ tự động khởi tạo hoá đơn thanh toán.
     *
     * @param e Thực thể Registration với thông tin ngày, người, và lớp
     * @throws Exception Các lỗi về giới hạn như học sinh đã đăng ký, lớp chưa được mở ...
     */
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

            Double courseFee = e.getClassEntity().getCourse().getFee();
            Invoice invoice = new Invoice(
                e,                          
                e.getStudent(),            
                courseFee,                 
                LocalDate.now(),           
                InvoiceStatus.Issued,      
                "Hóa đơn học phí khóa " + e.getClassEntity().getClassName()  
            );
            
            if (e.getClassEntity().getStatus() != ClassStatus.Open){
                throw new Exception("Lớp không mở để đăng ký!");
            }

            invoiceRepo.create(em, invoice);
            return null;
        });
    }

    /**
     * Cập nhật thông tin ghi danh.
     *
     * @param e Đối tượng Ghi danh
     * @throws Exception Lỗi cập nhật
     */
    public void update(Enrollment e) throws Exception {
        tx.runInTransaction(em -> {
            if(e.getStatus() == EnrollmentStatus.Dropped)
                e.setResult(ResultStatus.NA);

            repo.update(em,e);
            return null;
        });
    }

    /**
     * Xóa đăng ký học dựa theo định danh.
     *
     * @param id ID đăng ký
     * @throws Exception Các lỗi xóa dữ liệu khi có bảng tham chiếu
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            repo.delete(em,id);
            return null;
        });
    }

    /**
     * Đếm tổng số học viên trong lớp theo ID lớp.
     *
     * @param classId Cột định danh Class
     * @return Số lượng học viên học
     * @throws Exception Lỗi tính toán
     */
    public long countStudentsByClass(Long classId) throws Exception {
        return tx.runInTransaction(em ->
                repo.countStudentsByClass(em, classId)
        );
    }

    /**
     * Đăng ký khóa học dành cho học sinh, với kiểm tra sức chứa và trạng thái thực của lớp.
     *
     * @param studentId ID học sinh
     * @param clazz Lớp học cần đăng ký
     * @throws Exception Lỗi quá giới hạn, lỗi khoá học, lớp học
     */
    @Transactional
    public void register(Long studentId, Class clazz) throws Exception {
        tx.runInTransaction(em -> {
            if (clazz.getStatus() != ClassStatus.Open){
                throw new Exception("Lớp không mở để đăng ký!");
            }

            boolean exists = repo.existsStudentInCourse(
                    em,
                    studentId,
                    clazz.getCourse().getId()
            );

            if(exists)
                throw new Exception("Bạn đã đăng ký khóa học này rồi!");

            Long count = repo.countStudentsByClass(em, clazz.getId());

            if(count >= clazz.getMaxStudent())
                throw new Exception("Lớp đã đủ sinh viên!");

            Enrollment e = new Enrollment();
            e.setStudent(em.find(Student.class, studentId));
            e.setClassEntity(clazz);
            e.setEnrollmentDate(LocalDate.now());
            e.setStatus(EnrollmentStatus.Enrolled);
            e.setResult(ResultStatus.NA);

            repo.create(em, e);

            Double courseFee = e.getClassEntity().getCourse().getFee();
            Invoice invoice = new Invoice(
                e,                          
                e.getStudent(),            
                courseFee,                 
                LocalDate.now(),           
                InvoiceStatus.Issued,      
                "Hóa đơn học phí khóa " + e.getClassEntity().getClassName()  
            );

            invoiceRepo.create(em, invoice);
            return null;
        });
    }
}
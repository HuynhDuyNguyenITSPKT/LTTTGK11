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

public class EnrollmentService {

    private final EnrollmentRepository repo;
    private final InvoiceRepository invoiceRepo;
    private final TransactionManager tx;

    public EnrollmentService(EnrollmentRepository repo,
                            InvoiceRepository invoiceRepo,
                            TransactionManager tx) {
        this.repo = repo;
        this.invoiceRepo = invoiceRepo;
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

            // Insert enrollment
            repo.create(em,e);

            // Tự động tạo Invoice cho enrollment này
            // Lấy học phí từ Course
            Double courseFee = e.getClassEntity().getCourse().getFee();

            Invoice invoice = new Invoice(
                e,                          // enrollment
                e.getStudent(),            // student
                courseFee,                 // totalAmount = course fee
                LocalDate.now(),           // issueDate = now
                InvoiceStatus.Issued,      // status = Issued
                "Hóa đơn học phí khóa " + e.getClassEntity().getClassName()  // note
            );
            
            if (e.getClassEntity().getStatus() != ClassStatus.Open){
                throw new Exception("Lớp không mở để đăng ký!");
            }

            invoiceRepo.create(em, invoice);

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

    public long countStudentsByClass(Long classId) throws Exception {

        return tx.runInTransaction(em ->
                repo.countStudentsByClass(em, classId)
        );

    }

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

            // Tự động tạo Invoice cho enrollment này
            // Lấy học phí từ Course
            Double courseFee = e.getClassEntity().getCourse().getFee();

            Invoice invoice = new Invoice(
                e,                          // enrollment
                e.getStudent(),            // student
                courseFee,                 // totalAmount = course fee
                LocalDate.now(),           // issueDate = now
                InvoiceStatus.Issued,      // status = Issued
                "Hóa đơn học phí khóa " + e.getClassEntity().getClassName()  // note
            );

            invoiceRepo.create(em, invoice);

            return null;
        });
    }

}
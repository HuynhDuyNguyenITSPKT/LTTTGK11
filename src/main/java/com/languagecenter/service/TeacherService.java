package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Teacher;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.TeacherRepository;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

import java.util.List;

/**
 * Xử lý dữ liệu hồ sơ nhân sự (Giáo viên).
 * <p>
 * Class này đáp ứng chỉ tiêu quản lý thực thể Teacher độc lập 
 * cùng các tác vụ liên kết qua UserAccount (SRP).
 * </p>
 */
public class TeacherService {

    private final TeacherRepository teacherRepo;
    private final StudentRepository studentRepo;
    private final UserAccountRepository userRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo tính năng xử lý liên kết dữ liệu.
     *
     */
    public TeacherService(TeacherRepository teacherRepo,
                          StudentRepository studentRepo,
                          UserAccountRepository userRepo,
                          TransactionManager tx) {
        this.teacherRepo = teacherRepo;
        this.studentRepo = studentRepo;
        this.userRepo = userRepo;
        this.tx = tx;
    }

    /**
     * Lấy toàn bộ danh sách giáo viên.
     *
     */
    public List<Teacher> getAll() throws Exception {
        return tx.runInTransaction(em -> teacherRepo.findAll(em));
    }

    /**
     * Tuyển dụng thêm/Khởi tạo giáo viên mới cùng với tài khoản cá nhân.
     *
     */
    public void create(Teacher teacher,
                       String username,
                       String password) throws Exception {
        tx.runInTransaction(em -> {
            if(userRepo.findByUsername(em, username) != null){
                throw new Exception("Username đã tồn tại!");
            }

            if(teacher.getEmail() != null) {
                if(teacherRepo.findByEmail(em, teacher.getEmail()) != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                if(studentRepo.findByEmail(em, teacher.getEmail()) != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
            }

            teacherRepo.create(em, teacher);

            String hash = PasswordUtil.hash(password);
            UserAccount acc = new UserAccount(
                    username,
                    hash,
                    UserRole.Teacher,
                    true
            );

            acc.setTeacher(teacher);
            userRepo.save(em, acc);

            return null;
        });
    }

    /**
     * Chỉnh sửa thông tin hồ sơ và tài khoản của người dùng là giáo viên.
     *
     */
    public void update(Teacher teacher,
                       String username,
                       String password) throws Exception {
        tx.runInTransaction(em -> {
            if(userRepo.findByUsername(em, username) != null){
                UserAccount existingAcc = userRepo.findByUsername(em, username);
                if(!existingAcc.getTeacher().getId().equals(teacher.getId())){
                    throw new Exception("Username đã tồn tại!");
                }
            }

            if(teacher.getEmail() != null){
                Object existingTeacher = teacherRepo.findByEmail(em, teacher.getEmail());
                if(existingTeacher != null && !((Teacher) existingTeacher).getId().equals(teacher.getId())){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                Object existingStudent = studentRepo.findByEmail(em, teacher.getEmail());
                if(existingStudent != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
            }

            teacherRepo.update(em, teacher);

            UserAccount acc = userRepo.findByTeacherId(em, teacher.getId());

            if (acc != null) {
                acc.setUsername(username);
                if (password != null && !password.isBlank()) {
                    acc.setPasswordHash(PasswordUtil.hash(password));
                }
            }

            return null;
        });
    }

    /**
     * Bãi miễn hoặc xóa bản ghi về giáo viên này.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            UserAccount acc = userRepo.findByTeacherId(em, id);
            if (acc != null) {
                userRepo.delete(em, acc.getId());
            }
            teacherRepo.delete(em, id);
            return null;
        });
    }

    /**
     * Tra cứu tài khoản theo định danh giảng viên đó.
     *
     */
    public UserAccount findAccountByTeacherId(Long id) throws Exception {
        return tx.runInTransaction(em -> userRepo.findByTeacherId(em, id));
    }
}
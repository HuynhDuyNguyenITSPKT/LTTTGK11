package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Student;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.TeacherRepository;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

import java.util.List;

/**
 * Xử lý dữ liệu học sinh cùng tài khoản liên kết trong hệ thống.
 * <p>
 * Class thể hiện trách nhiệm bảo vệ toàn vẹn dữ liệu lúc khởi tạo và
 * quản trị hồ sơ của Học Sinh.
 * </p>
 */
public class StudentService {
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final UserAccountRepository userRepo;
    private final TransactionManager tx;

    /**
     * Khởi tạo tính năng xử lý liên kết dữ liệu Học Sinh.
     *
     */
    public StudentService(StudentRepository studentRepo,
                         TeacherRepository teacherRepo,
                         UserAccountRepository userRepo,
                         TransactionManager tx) {
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
        this.tx = tx;
    }

    /**
     * Lấy toàn bộ danh sách sinh viên hiện có.
     *
     */
    public List<Student> getAll() throws Exception{
        return tx.runInTransaction(
                em -> studentRepo.findAll(em)
        );
    }

    /**
     * Khởi tạo thông tin sinh viên kèm một tài khoản đăng nhập tương ứng.
     *
     */
    public void create(Student student, String username, String password) throws Exception {
        tx.runInTransaction(em -> {
            if(userRepo.findByUsername(em, username) != null){
                throw new Exception("Username đã tồn tại!");
            }

            if(student.getEmail() != null) {
                if(studentRepo.findByEmail(em, student.getEmail()) != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                if(teacherRepo.findByEmail(em, student.getEmail()) != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
            }

            studentRepo.create(em, student);
            String hash = PasswordUtil.hash(password);
            UserAccount acc = new UserAccount(
                    username,
                    hash,
                    UserRole.Student,
                    true
            );
            acc.setStudent(student);
            userRepo.save(em, acc);
            return null;
        });
    }

    /**
     * Sửa đổi dữ liệu của tài khoản cùng thông tin hồ sơ của sinh viên.
     *
     */
    public void update(Student student,
                       String username,
                       String password) throws Exception {

        tx.runInTransaction(em -> {

            String currentUsername = userRepo.findByStudentId(em, student.getId()).getUsername();
            if(!currentUsername.equals(username) && userRepo.findByUsername(em, username) != null){
                throw new Exception("Username đã tồn tại!");
            }

            if(student.getEmail() != null){
                Object existingStudent = studentRepo.findByEmail(em, student.getEmail());
                if(existingStudent != null && !((Student) existingStudent).getId().equals(student.getId())){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                Object existingTeacher = teacherRepo.findByEmail(em, student.getEmail());
                if(existingTeacher != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
            }

            studentRepo.update(em, student);
            UserAccount acc = userRepo.findByStudentId(em, student.getId());

            if(acc != null){
                acc.setUsername(username);
                if(password != null && !password.isBlank()){
                    acc.setPasswordHash(PasswordUtil.hash(password));
                }
            }

            return null;
        });
    }

    /**
     * Thu hồi dữ liệu học sinh cùng tài khoản liên kết có trong hệ thống CSDL.
     *
     */
    public void delete(Long id) throws Exception {
        tx.runInTransaction(em -> {
            UserAccount acc = userRepo.findByStudentId(em,id);
            if(acc!=null){
                userRepo.delete(em,acc.getId());
            }
            studentRepo.delete(em,id);
            return null;
        });
    }

    /**
     * Tra cứu tài khoản đăng nhập mà học sinh này đang giữ.
     *
     */
    public UserAccount findAccountByStudentId(Long id) throws Exception{
        return tx.runInTransaction(em -> userRepo.findByStudentId(em,id));
    }
}

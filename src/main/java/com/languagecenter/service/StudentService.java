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
     * @param studentRepo Repo thuộc học sinh
     * @param teacherRepo Repo thuộc giáo viên (để kiểm tra các trường duy nhất)
     * @param userRepo    Repo về tài khoản đăng nhập
     * @param tx          Phân luồng transaction
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
     * @return Danh sách Student
     * @throws Exception Lỗi mạng
     */
    public List<Student> getAll() throws Exception{
        return tx.runInTransaction(
                em -> studentRepo.findAll(em)
        );
    }

    /**
     * Khởi tạo thông tin sinh viên kèm một tài khoản đăng nhập tương ứng.
     *
     * @param student Thực thể thông tin cá nhân
     * @param username Tên truy cập mong muốn
     * @param password Mật khẩu
     * @throws Exception Trả về khi Username/Email đã tồn tại
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
     * @param student Cập nhật thông tin sinh viên
     * @param username Tên mới
     * @param password Mật khẩu (nếu có để đổi)
     * @throws Exception Các lỗi về giới hạn trùng thông tin
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
     * @param id Khóa chính học sinh
     * @throws Exception Lỗi phá vỡ ràng buộc khóa liên kết
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
     * @param id ID định danh cá nhân
     * @return Tài khoản tham chiếu
     * @throws Exception Các lỗi từ Database
     */
    public UserAccount findAccountByStudentId(Long id) throws Exception{
        return tx.runInTransaction(em -> userRepo.findByStudentId(em,id));
    }
}

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

public class StudentService {
    private final StudentRepository studentRepo;
    private final TeacherRepository teacherRepo;
    private final UserAccountRepository userRepo;
    private final TransactionManager tx;

    public StudentService(StudentRepository studentRepo,
                         TeacherRepository teacherRepo,
                         UserAccountRepository userRepo,
                         TransactionManager tx) {
        this.studentRepo = studentRepo;
        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
        this.tx = tx;
    }

    public List<Student> getAll() throws Exception{
        return tx.runInTransaction(
                em -> studentRepo.findAll(em)
        );
    }

    public void create(Student student, String username, String password) throws Exception {
        tx.runInTransaction(em -> {
            if(userRepo.findByUsername(em, username) != null){
                throw new Exception("Username đã tồn tại!");
            }

            // Kiểm tra email trùng trong cả Student VÀ Teacher
            if(student.getEmail() != null) {
                // Kiểm tra trong Student
                if(studentRepo.findByEmail(em, student.getEmail()) != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                // Kiểm tra trong Teacher
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

    public void update(Student student,
                       String username,
                       String password) throws Exception {

        tx.runInTransaction(em -> {

            String currentUsername = userRepo.findByStudentId(em, student.getId()).getUsername();
            if(!currentUsername.equals(username) && userRepo.findByUsername(em, username) != null){
                throw new Exception("Username đã tồn tại!");
            }

            // Kiểm tra email trùng trong cả Student VÀ Teacher (ngoại trừ chính student này)
            if(student.getEmail() != null){
                // Kiểm tra trong Student
                Object existingStudent = studentRepo.findByEmail(em, student.getEmail());
                if(existingStudent != null && !((Student) existingStudent).getId().equals(student.getId())){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
                // Kiểm tra trong Teacher
                Object existingTeacher = teacherRepo.findByEmail(em, student.getEmail());
                if(existingTeacher != null){
                    throw new Exception("Email đã tồn tại trong hệ thống!");
                }
            }

            studentRepo.update(em, student);

            UserAccount acc =
                    userRepo.findByStudentId(em, student.getId());

            if(acc != null){

                acc.setUsername(username);

                if(password != null && !password.isBlank()){

                    acc.setPasswordHash(
                            PasswordUtil.hash(password)
                    );
                }
            }

            return null;
        });
    }

    public void delete(Long id) throws Exception {

        tx.runInTransaction(em -> {
            UserAccount acc =
                    userRepo.findByStudentId(em,id);
            if(acc!=null){
                userRepo.delete(em,acc.getId());
            }
            studentRepo.delete(em,id);
            return null;
        });
    }

    public UserAccount findAccountByStudentId(Long id) throws Exception{
        return tx.runInTransaction(em ->
                    userRepo.findByStudentId(em,id));
    }
}

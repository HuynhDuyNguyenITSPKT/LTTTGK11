package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Student;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.repo.StudentRepository;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

import java.util.List;

public class StudentService {
    private final StudentRepository studentRepo;
    private final UserAccountRepository userRepo;
    private final TransactionManager tx;

    public StudentService(StudentRepository studentRepo, UserAccountRepository userRepo, TransactionManager tx) {
        this.studentRepo = studentRepo;
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

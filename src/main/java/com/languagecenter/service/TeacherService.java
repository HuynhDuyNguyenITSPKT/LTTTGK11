package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.model.Teacher;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.UserRole;
import com.languagecenter.repo.TeacherRepository;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

import java.util.List;

public class TeacherService {

    private final TeacherRepository teacherRepo;
    private final UserAccountRepository userRepo;
    private final TransactionManager tx;

    public TeacherService(TeacherRepository teacherRepo,
                          UserAccountRepository userRepo,
                          TransactionManager tx) {

        this.teacherRepo = teacherRepo;
        this.userRepo = userRepo;
        this.tx = tx;
    }

    public List<Teacher> getAll() throws Exception {

        return tx.runInTransaction(
                em -> teacherRepo.findAll(em)
        );
    }

    public void create(Teacher teacher,
                       String username,
                       String password) throws Exception {

        tx.runInTransaction(em -> {

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

    public void update(Teacher teacher,
                       String username,
                       String password) throws Exception {

        tx.runInTransaction(em -> {

            teacherRepo.update(em, teacher);

            UserAccount acc =
                    userRepo.findByTeacherId(em, teacher.getId());

            if (acc != null) {

                acc.setUsername(username);

                if (password != null && !password.isBlank()) {

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
                    userRepo.findByTeacherId(em, id);

            if (acc != null) {
                userRepo.delete(em, acc.getId());
            }

            teacherRepo.delete(em, id);

            return null;
        });
    }

    public UserAccount findAccountByTeacherId(Long id) throws Exception {

        return tx.runInTransaction(
                em -> userRepo.findByTeacherId(em, id)
        );
    }
}
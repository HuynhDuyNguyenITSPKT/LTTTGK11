package com.languagecenter.service;

import com.languagecenter.db.TransactionManager;
import com.languagecenter.init.Logger;
import com.languagecenter.init.logger.ConsoleLogger;
import com.languagecenter.init.logger.fac.ConsoleFac;
import com.languagecenter.init.logger.fac.FileLogFac;
import com.languagecenter.init.logger.fac.LogFacSer;
import com.languagecenter.model.UserAccount;
import com.languagecenter.model.enums.StudentStatus;
import com.languagecenter.model.enums.TeacherStatus;
import com.languagecenter.repo.UserAccountRepository;
import com.languagecenter.util.PasswordUtil;

public class AuthService {

    private final UserAccountRepository repo;
    private final TransactionManager tx;

    public AuthService(UserAccountRepository repo,TransactionManager tx){
        this.repo = repo;
        this.tx = tx;
    }

    public UserAccount login(String username,String password) throws Exception{

        return tx.runInTransaction(em -> {
                UserAccount user =
                        repo.findByUsername(em, username);

                if (user == null) {
                    throw new RuntimeException("Invalid username or password");
                }

                if (!Boolean.TRUE.equals(user.getIsActive())) {
                    throw new RuntimeException("Account disabled");
                }

                if(user.getStudent() != null && user.getStudent().getStatus() != StudentStatus.Active){
                    throw new RuntimeException("Student account is not active");
                }

                if(user.getTeacher() != null && user.getTeacher().getStatus() != TeacherStatus.Active){
                    throw new RuntimeException("Teacher account is not active");
                }

                boolean match =
                        PasswordUtil.verify(password, user.getPasswordHash());

                if (!match) {
                    throw new RuntimeException("Invalid username or password");
                }

                Logger a = LogFacSer.getFileLogger(new ConsoleFac());

                a.log(username + " login ");

                Logger b = LogFacSer.getFileLogger(new FileLogFac());

                b.log(username + " login with " + password );

                return user;
            });
    }
}